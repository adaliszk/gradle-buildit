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
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File

class HytaleDevServerRun : HytaleGradle.ConfigureIdeaDev {
    private val log: Logger = Logging.getLogger(this::class.java)

    override var devserverDir: String = "devserver"

    internal var devserver: DevServerConfig = DevServerConfig()

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
        configureIdea()

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
            doLast {
                bootstrapDevserver()
                bootstrapAssets()
            }
        }
    }

    private fun isIdeaSync(): Boolean {
        // IntelliJ sets these during Gradle sync
        return System.getProperty("idea.sync.active")?.toBoolean() == true
            || project.gradle.startParameter.taskNames.any {
            it.contains("idea", ignoreCase = true) || it.contains("processIdeaSettings")
        }
    }

    fun configureIdea() {
        if (!isIdeaSync()) return

        val manifest = HytaleManifest.from(project)
        val packageCandidates = listOfNotNull(
            manifest.Main?.substringBeforeLast("."),
            "${project.group}.${project.name}".replace(":", ".").trim('.'),
            "${project.rootProject.name}",
        )

        project.plugins.apply(IdeaExtPlugin::class.java)
        project.extensions.configure<IdeaModel>("idea") { idea ->
            val ideaProject = idea.project ?: return@configure
            ideaProject.settings.runConfigurations {
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

    private fun bootstrapDevserver() {
        if (!devserver.Enabled) return

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

    private fun bootstrapAssets() {
        resolveAssets()
    }

    private fun resolveAssets(): File {
        val patchline = HytaleExtension.patchline
        val version = HytaleExtension.version.replace("+", "latest")

        val downloadPath = homePath.resolve("$version/Assets.zip")
        log.debug("> Hytale :${project.name}.downloadPath: ${downloadPath.canonicalPath}")

        val installPath = homePath.resolve("install/$patchline/package/game/$version/Assets.zip")
        log.debug("> Hytale :${project.name}.installPath: ${installPath.canonicalPath}")

        return when (true) {
            downloadPath.exists() -> downloadPath
            installPath.exists() -> installPath
            else -> throw GradleException(
                "Assets are not present, without that it is not possible to run a server! " +
                    "Please download the ${HytaleExtension.patchline} via the Hytale Launcher " +
                    "which should download into `$homePath`, but you can overwrite that with " +
                    "the `hytale.home_path` gradle.properties entry!"
            )
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
        val devServerDCEVM = project.providers
            .gradleProperty("env.hytale.devServerDCEVM")
            .getOrElse("true").toBoolean()

        // Project access is forbidden from Gradle 10 onwards, so these need to be set outside
        // the task context itself:

        val devserverPath = project.file(devserverDir)

        val runtimeClassPath = project.configurations.getByName("runtimeClasspath")
        val projectOutput = project.extensions
            .getByType(SourceSetContainer::class.java)
            .getByName("main").output

        val jvmArguments = mutableListOf<String>()
        val javaProvider = resolveJava()

        val manifestTask = project.tasks.named("generateManifest")
        val buildTask = project.tasks.named("classes")

        val serverArgs = createServerRunArgumentsList()
        log.lifecycle("> Hytale: :runServer:$serverArgs")

        val runServer = project.tasks.maybeCreate("runServer", JavaExec::class.java).apply {
            description =
                "Runs the devserver, use -Ddebug for opening a debugger and allow hot-swapping"
            group = "hytale"
            dependsOn(manifestTask, buildTask)

            standardInput = System.`in`
            classpath(runtimeClassPath, projectOutput)
            workingDir(devserverPath)
            mainClass.set("com.hypixel.hytale.Main")

            if (javaProvider.hasDCEVM && devServerDCEVM) {
                jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
            }

            if (System.getProperty("debug") != null) {
                jvmArguments.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
                jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
            }

            javaLauncher.set(javaProvider.jdk)

            jvmArguments.add("-XX:+EnableDynamicAgentLoading")
            jvmArgs(jvmArguments)
            args(serverArgs)

            doLast {
                if (!devserverPath.exists()) {
                    throw GradleException(
                        "Devserver has not been initialized in $devserverDir yet, " +
                            "please run ./gradlew setupServer to initialize it!"
                    )
                }
            }
        }

        project.tasks.maybeCreate("devServer").apply {
            description = "Alias for runServer for us who forget sometimes"
            group = "hytale"
            dependsOn(runServer)
        }
    }

    private fun generateIdeaRunConfiguration() {
        val configureIdea = project.providers
            .gradleProperty("env.hytale.configureIdea")
            .getOrElse("true").toBoolean()
        if (!configureIdea) return

        val devServerDCEVM = project.providers
            .gradleProperty("env.hytale.devServerDCEVM")
            .getOrElse("true").toBoolean()

        val mainPackage: String = HytaleManifest.from(project).Main?.substringBeforeLast(".")
            ?: project.rootProject.name

        val javaProvider = resolveJava()

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
                        config.programParameters = createServerRunArgumentsString()
                        config.workingDirectory = project.file(devserverDir).absolutePath
                        if (javaProvider.hasDCEVM && devServerDCEVM) {
                            config.jvmArgs = "-XX:+AllowEnhancedClassRedefinition -XX:+EnableDynamicAgentLoading"
                        }
                    }
                }
            }
        }
    }

    private fun createServerRunArgumentsString(): String {
        return createServerRunArguments().map { (key, value) ->
            if (value != null) "$key=${value.quotedIfNeeded()}" else key
        }.joinToString(" ")
    }

    private fun createServerRunArgumentsList(): List<String> {
        return createServerRunArguments().map { (key, value) ->
            if (value != null) "$key=${value}" else key
        }
    }

    private fun createServerRunArguments(): Map<String, String?> {
        val assetsFile = resolveAssets()
        val params = devserver.toArgs().toMutableMap()
        params["--assets"] = "${assetsFile.canonicalPath}"
        val modPaths = mutableListOf<String>().also {
            it.add(sourcePath.canonicalPath)
            if (devserver.IncludeUserMods) {
                // TODO: Check that the launcher instance is even installed, and if there are duplicates
                it.add(File("${homePath}/UserData/Mods").canonicalPath)
            }
        }
        params["--mods"] = modPaths.joinToString(",")
        return params.toMap()
    }

    private fun String.quotedIfNeeded(): String =
        if (contains(" ")) '"' + this + '"' else this

    private data class JavaProvider(
        val jdk: Provider<JavaLauncher>, val hasDCEVM: Boolean
    )

    private fun resolveJava(): JavaProvider {
        val toolchains = project.extensions.getByType(JavaToolchainService::class.java)

        val jetbrainsJDK = toolchains.launcherFor {
            it.languageVersion.set(JavaLanguageVersion.of(25))
            it.vendor.set(JvmVendorSpec.JETBRAINS)
        }

        val fallbackJDK = toolchains.launcherFor {
            it.languageVersion.set(JavaLanguageVersion.of(25))
        }

        return try {
            jetbrainsJDK.get() // forces resolution so it can throw errors like it does not exist
            JavaProvider(jetbrainsJDK, true)
        } catch (_: Exception) {
            log.warn("JetBrains JDK not found, falling back to default JDK 25. DCEVM hot-swap won't be available.")
            // TODO: Communicate this to the dependencies too so that the Devtools Agent also not added
            JavaProvider(fallbackJDK, false)
        }
    }

    // endregion
}