package dev.buildit.gradle.hytale

import dev.buildit.gradle.BuildItExtension
import dev.buildit.gradle.GradleExtension
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Creates an environment to build hytale mods
 */
@Suppress("unused") // Used by Gradle, but that is not visible
open class HytaleExtension(pending: Lazy<Project>) : GradleExtension(pending) {
    private val log: Logger = Logging.getLogger(HytaleExtension::class.java)

    override fun onInitialize() {
        register("hytale") {
            validateHytaleInstallation()
            configureDependencies()
            configureManifestTask()
            libs.forEach {
                project.dependencies.add("implementation", project.project(it))
            }
            prepareWorkspace()
        }
    }

    fun includeAssetPack(shouldInclude: Boolean = true) {
        // TODO
    }

    val patchline: String by lazy {
        project.providers.gradleProperty("hytale.patchline")
            .orElse("release").get()
    }

    val hytaleHome: String by lazy {
        val home = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()

        when {
            project.hasProperty("hytale.home_path") ->
                project.findProperty("hytale.home_path") as String

            osName.contains("win") ->
                "$home/AppData/Roaming/Hytale"

            osName.contains("mac") ->
                "$home/Library/Application Support/Hytale"

            osName.contains("nix") || osName.contains("nux") -> {
                val flatpak = "$home/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
                if (project.file(flatpak).exists()) flatpak
                else "$home/.local/share/Hytale"
            }

            else -> error("Unsupported OS for Hytale home resolution: $osName")
        }
    }

    val serverJar: String by lazy {
        val installed =
            project.file("$hytaleHome/install/$patchline/package/game/latest/Server/HytaleServer.jar")
        require(installed.isFile) { "HytaleServer.jar not found: ${installed.absolutePath}" }
        installed.absolutePath
        // TODO: Use once there is a way to download the particular patchline
        // installed.takeIf { it.exists() }?.absolutePath ?: run {
        //    File(serverRunDir, "HytaleServer.jar").also { downloaded ->
        //        URI("https://cdn.hytale.com/HytaleServer.jar").toURL().openStream().use {
        //            it.copyTo(downloaded.outputStream())
        //        }
        //    }.absolutePath
        // }
    }

    private fun validateHytaleInstallation() {
        when {
            !project.file(hytaleHome).exists() -> throw GradleException(
                "Failed to find Hytale at the expected location. " +
                    "Please make sure you have installed the game. " +
                    "The expected location can be changed using the hytale.home_path property. " +
                    "Currently looking in $hytaleHome",
            )

            !project.file(serverJar).exists() -> throw GradleException(
                "Failed to find HytaleServer.jar at the expected location. " +
                    "Expected: $serverJar. " +
                    "This file is required for running a development server. " +
                    "Please ensure your Hytale installation is complete ($patchline). " +
                    "You may need to repair or reinstall Hytale through the launcher.",
            )
        }
    }

    private fun configureDependencies() {
        with(project.dependencies) {
            add("implementation", project.files(serverJar))
        }
    }

    private fun configureManifestTask() {
        val updateManifest = project.tasks.findByName("updatePluginManifest")
            ?: project.tasks.register("updatePluginManifest") { task ->
                task.doFirst {
                    HytaleManifest.from(project).update()
                }
            }
        project.afterEvaluate {
            project.tasks.named("processResources", Copy::class.java) { task ->
                task.dependsOn(updateManifest)
                task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }

    private fun prepareWorkspace() {
        when (BuildItExtension.kotlinLibrary) {
            "org.jetbrains.kotlin:kotlin-stdlib" -> prepareKotlinWorkspace()
            "" -> prepareJavaWorkspace()
            else -> log.warn(
                "Bootstrapping skipped, unsupported Kotlin library: ${BuildItExtension.kotlinLibrary}"
            )
        }
    }

    private val packagePath by lazy {
        mainClass.substringBeforeLast('.').replace('.', '/')
    }

    private val className by lazy {
        mainClass.substringAfterLast('.')
    }

    private fun prepareJavaWorkspace() {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val javaDir = sourceSets.getByName("main").java.srcDirs.first()
        project.copy {
            it.from(
                project.resources.text.fromUri(
                    javaClass.getResource("/hytale/java/HytalePlugin.tpl")
                )
            )
            it.into(javaDir.resolve(packagePath))
            it.expand(
                mapOf(
                    "packageName" to mainClass.substringBeforeLast('.'),
                    "className" to className,
                ),
            )
            it.rename { "$className.java" }
        }
    }

    private fun prepareKotlinWorkspace() {
        if (!project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) return

        val sourceSets =
            project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
        val kotlinDir = sourceSets.getByName("main").kotlin.srcDirs.first()
        project.copy {
            it.from(
                project.resources.text.fromUri(
                    javaClass.getResource("/hytale/kotlin/HytalePlugin.tpl")
                )
            )
            it.into(kotlinDir.resolve(packagePath))
            it.expand(
                mapOf(
                    "packageName" to mainClass.substringBeforeLast('.'),
                    "className" to className,
                ),
            )
            it.rename { "$className.kt" }
        }
    }
}