// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.Language
import dev.scaffoldit.hytale.wire.DevServerConfig
import dev.scaffoldit.hytale.wire.HytaleGradle
import dev.scaffoldit.hytale.wire.HytaleManifest
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.JavaExec
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.IdeaExtPlugin
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File
import kotlin.apply

class HytaleDevServerRun : HytaleGradle.ConfigureIdeaDev {
    private val log: Logger = Logging.getLogger(this::class.java)

    override var devserverDir: String = "devserver"

    internal var devserver: DevServerConfig? = null

    override fun devserver(config: DevServerConfig.() -> Unit) {
        devserver = DevServerConfig().apply(config)
    }

    override fun devserver(action: Action<DevServerConfig>) {
        devserver = DevServerConfig().also { action.execute(it) }
    }

    // region: Internal APIs

    lateinit var project: Project
    var kotlin: String? = null

    fun configure(project: Project, parent: Gradle.ConfigureToolchain): Gradle.ConfigureIdeaDev {
        log.debug(":${project.name}:HytaleDevserverRun(project)")
        this.kotlin = parent.kotlin
        this.project = project

        registerSetupTask()
        registerRunTask()

        val manifest = HytaleManifest.from(project)

        val packageCandidates = listOfNotNull(
            manifest.Main?.substringBeforeLast("."),
            "${project.group}.${project.name}".replace(":", ".").trim('.'),
            "${project.rootProject.name}",
        )

        project.afterEvaluate { scope ->
            if (scope.path !== project.path) return@afterEvaluate

            with(project.gradle.rootProject) {
                plugins.apply(IdeaExtPlugin::class.java)
                extensions.configure<IdeaModel>("idea") { idea ->
                    idea.project.settings.runConfigurations {
                        val config = withType(Application::class.java).firstOrNull {
                            packageCandidates.contains("${it.moduleName}.main")
                        }

                        if (config == null) {
                            val task = project.tasks.named("setupServer").get()
                            task.actions.forEach { it.execute(task) }
                            generateIdeaRunConfiguration()
                        }
                    }
                }
            }
        }

        return this
    }

    private val homePath: File by lazy {
        val home = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()

        when {
            project.hasProperty("hytale.home_path") ->
                File(project.findProperty("hytale.home_path") as String)

            osName.contains("win") ->
                File("$home/AppData/Roaming/Hytale")

            osName.contains("mac") ->
                File("$home/Library/Application Support/Hytale")

            osName.contains("nix") || osName.contains("nux") -> {
                val flatpak = File("$home/.var/app/com.hypixel.HytaleLauncher/data/Hytale")
                if (flatpak.exists()) flatpak
                else File("$home/.local/share/Hytale")
            }

            else -> project.rootProject.buildFile.resolve("hytale")
                .also { it.mkdirs() }
        }
    }

    fun registerSetupTask() {
        project.tasks.maybeCreate("setupServer").apply {
            description = "(Re-)Create the the devserver within the project"
            group = "hytale"
            bootstrapDevserver()
            bootstrapAssets()
        }
    }

    private fun bootstrapDevserver() {
        project.afterEvaluate {
            if (devserver?.Enabled == false) return@afterEvaluate // Feature-flag

            val serverRunDir = project.file(devserverDir)
            if (serverRunDir.mkdirs()) {
                javaClass.getResourceAsStream("/server.zip")?.use { stream ->
                    project.zipTree(
                        project.file("temp.zip").apply { writeBytes(stream.readBytes()) })
                        .let { tree -> project.copy { it.from(tree); it.into(serverRunDir) } }
                        .also { project.delete("temp.zip") }
                }
            }
        }
    }

    private fun bootstrapAssets() {
        val assetsFile = resolveAssets()
        if (assetsFile !== null) return // Already exist no need to download

        // TODO: Implement downloading via the installer when no instance available

        throw GradleException(
            "Assets are not present, without that it is not possible to run a server! " +
                "Please download the ${HytaleExtension.patchline} via the Hytale Launcher " +
                "which should download into `$homePath`, but you can overwrite that with " +
                "the `hytale.home_path` gradle.properties entry!"
        )
    }

    private fun resolveAssets(): File? {
        val patchline = HytaleExtension.patchline
        val version = HytaleExtension.version.replace("+", "latest")

        val downloadPath = homePath.resolve("$version/Assets.zip")
        log.lifecycle("> Hytale :${project.name}.downloadPath: ${downloadPath.canonicalPath}")

        val installPath = homePath.resolve("install/$patchline/package/game/$version/Assets.zip")
        log.lifecycle("> Hytale :${project.name}.installPath: ${installPath.canonicalPath}")

        return when (true) {
            downloadPath.exists() -> downloadPath
            installPath.exists() -> installPath
            else -> null
        }
    }

    private val sourcePath: File by lazy {
        val language = if (kotlin !== null) Language.KOTLIN else Language.JAVA
        when (language) {
            Language.KOTLIN -> project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
                .getByName("main").kotlin.srcDirs.first().parentFile

            Language.JAVA -> project.extensions.getByType(SourceSetContainer::class.java)
                .getByName("main").java.srcDirs.first().parentFile
        }
    }

    fun registerRunTask() {
        project.tasks.register("runServer", JavaExec::class.java) {
            it.description =
                "Runs the devserver, use -Ddebug for opening a debugger and allow hot-swapping"
            it.group = "hytale"

            it.doFirst { t ->
                if (!project.file(devserverDir).exists()) {
                    throw GradleException(
                        "Devserver has not be initialized in $devserverDir yet, " +
                            "please run ./gradlew setupServer to initialize it!"
                    )
                }
                val jExec = t as JavaExec
                val exec = jExec.executable ?: "java"
                val jvm = jExec.allJvmArgs.joinToString(" ")
                val cp = jExec.classpath.asPath
                val main = jExec.mainClass.get()
                val args = jExec.args?.joinToString(" ") ?: ""
                log.lifecycle("Running Exec: $exec $jvm -cp \"$cp\" $main $args")
            }

            it.mainClass.set("com.hypixel.hytale.Main")
            it.classpath = project.extensions.getByType(SourceSetContainer::class.java)
                .getByName("main").runtimeClasspath
            it.workingDir = project.file(devserverDir)
            it.args = createServerRunArgumentsList()
            
            it.standardInput = System.`in`

            val jvmArguments = mutableListOf<String>()
            val devServerDCEVM = project.providers
                .gradleProperty("env.hytale.devServerDCEVM")
                .getOrElse("false").toBoolean()

            if (devServerDCEVM) {
                jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
            }

            if (System.getProperty("debug") != null) {
                jvmArguments.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            }

            it.jvmArgs = jvmArguments
        }
    }

    private fun generateIdeaRunConfiguration() {
        val configureIdea = project.providers
            .gradleProperty("env.hytale.configureIdea")
            .getOrElse("true").toBoolean()
        if (!configureIdea) return

        val devServerDCEVM = project.providers
            .gradleProperty("env.hytale.devServerDCEVM")
            .getOrElse("false").toBoolean()

        val mainPackage: String = HytaleManifest.from(project).Main?.substringBeforeLast(".")
            ?: project.rootProject.name ?: "${project.group}.${project.name}"

        with(project) {
            plugins.apply(IdeaExtPlugin::class.java)
            extensions.configure(IdeaModel::class.java) { idea ->
                val ideaProject = idea.project ?: return@configure
                (ideaProject as ExtensionAware).extensions.configure(ProjectSettings::class.java) { settings ->
                    settings.runConfigurations.create(
                        "${project.name.substringAfterLast('.')}:devserver",
                        Application::class.java
                    ) { config ->
                        config.mainClass = "com.hypixel.hytale.Main"
                        config.moduleName = "${mainPackage}.main".removePrefix(".")
                        config.programParameters = createServerRunArguments()
                        config.workingDirectory = project.file(devserverDir).absolutePath
                        if (devServerDCEVM) {
                            config.jvmArgs = "-XX:+AllowEnhancedClassRedefinition"
                        }
                    }
                }
            }
        }
    }

    private fun createServerRunArguments(): String {
        return createServerRunArgumentsList().joinToString(" ")
    }

    private fun createServerRunArgumentsList(): List<String> {
        val assetsFile = resolveAssets()
        val params = devserver?.toArgs()?.toMutableList()
            ?: mutableListOf()
        params.add("--assets=$assetsFile")
        val modPaths = mutableListOf<String>().also {
            it.add(sourcePath.absolutePath)
            if (devserver?.IncludeUserMods == true) {
                // TODO: Check that the launcher instance is even installed
                it.add("${homePath}/UserData/Mods")
            }
        }
        params.add("--mods=${modPaths.joinToString(",")}")
        return params
    }

    // endregion
}