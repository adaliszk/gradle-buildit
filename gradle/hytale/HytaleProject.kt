package dev.buildit.hytale

import dev.buildit.gradle.CraftableExtension
import dev.buildit.gradle.GradleExtension
import dev.buildit.gradle.ProjectMetadata
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Creates an environment to build hytale mods
 */
open class HytaleProject(settings: Settings) : CraftableExtension(settings)
{
    var includeAssetPack: Boolean = true

    val patchline: String by lazy {
        project.providers.gradleProperty("hytale.patchline")
            .orElse("release").get()
    }

    val hytaleHome: String by lazy {
        val home = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()

        when
        {
            project.hasProperty("hytale.home_path") ->
                project.findProperty("hytale.home_path") as String

            osName.contains("win") ->
                "$home/AppData/Roaming/Hytale"

            osName.contains("mac") ->
                "$home/Library/Application Support/Hytale"

            osName.contains("nix") || osName.contains("nux") ->
            {
                val flatpak = "$home/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
                if (project.file(flatpak).exists()) flatpak
                else "$home/.local/share/Hytale"
            }

            else -> error("Unsupported OS for Hytale home resolution: $osName")
        }
    }

//    val serverRunDir by lazy {
//        project.file("${project.projectDir}/devserver").apply {
//            if (!exists()) mkdirs()
//        }
//    }

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

    override fun configureSettings()
    {
        settings.rootDir
            .resolve("targets/hytale/devserver")
            .mkdirs()

        register(":targets:hytale", hytaleTarget()) {
            libs.forEach {
                project.dependencies.add(
                    "implementation",
                    project.project(it),
                )
            }
        }
    }

    private fun hytaleTarget() = ProjectMetadata(
        packageName = settings.providers.gradleProperty("project.name").get(),
        type = ProjectMetadata.Type.HYTALE,
        options = mapOf(
            "includeAssetPack" to includeAssetPack,
        )
    )

    override fun configure(target: Project)
    {
        // if (project.path !== target.path) return

        validateHytaleInstallation()
        configureManifestTask(target)
        configureDependencies(target)
        target.afterEvaluate {
            prepareWorkspace(target)
        }
    }

    private fun configureDependencies(target: Project)
    {
        target.afterEvaluate {
            target.dependencies.apply {
                add("implementation", target.files(serverJar))
            }
        }
    }

    private fun prepareWorkspace(target: Project)
    {
        HytalePluginManifest.from(target).update()
    }

    private fun validateHytaleInstallation()
    {
        when
        {
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

    private fun configureManifestTask(target: Project)
    {
        val updateManifest = target.tasks
            .register("updatePluginManifest") { task ->
                task.doFirst {
                    HytalePluginManifest.from(target).update()
                }
            }

        target.afterEvaluate {
            target.tasks.named("processResources", Copy::class.java) { task ->
                task.dependsOn(updateManifest)
                task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}