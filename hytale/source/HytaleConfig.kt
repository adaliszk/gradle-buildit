// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import org.gradle.api.GradleException
import org.gradle.api.Project

data class HytaleConfig(
    private val project: Project,
) {
    val patchline: String by lazy {
        project.providers.gradleProperty("hytale.patchline")
            .orElse("release").get()
    }

    val homePath: String by lazy {
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

    val serverFilePath: String by lazy {
        val installed =
            project.file("$homePath/install/$patchline/package/game/latest/Server/HytaleServer.jar")
        require(installed.isFile) { "HytaleServer.jar not found: ${installed.absolutePath}" }
        installed.absolutePath
    }

    // TODO: Use the overwritten manifest from Wired to provide a default
    val mainPackage: String = "${project.group}.${project.name}"
        .replace("-", ".")
        .replace("_", ".")
        .lowercase()

    // TODO: Use the overwritten manifest from Wired to provide a default
    val mainClassName: String = project.name.replaceFirstChar { it.uppercase() } + "Plugin"

    fun validateHytaleInstallation() {
        when {
            !project.file(homePath).exists() -> throw GradleException(
                "Failed to find Hytale at the expected location. " +
                    "Please make sure you have installed the game. " +
                    "The expected location can be changed using the hytale.home_path property. " +
                    "Currently looking in $homePath",
            )

            !project.file(serverFilePath).exists() -> throw GradleException(
                "Failed to find HytaleServer.jar at the expected location. " +
                    "Expected: $serverFilePath. " +
                    "This file is required for running a development server. " +
                    "Please ensure your Hytale installation is complete ($patchline). " +
                    "You may need to repair or reinstall Hytale through the launcher.",
            )
        }
    }
}