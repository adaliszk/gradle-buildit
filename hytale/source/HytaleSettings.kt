// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.extraProperties

/**
 * Configure a Hytale project with:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(settings),
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Gradle.ConfigurePlatform by HytaleServerPlatform(),
    Gradle.ConfigureIdeaDev by HytaleDevserverRun(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = ""

    private var manifestConfig: (HytaleManifest.() -> Unit)? = null

    fun manifest(config: HytaleManifest.() -> Unit) {
        log.lifecycle("> Plug :hytale(settings):manifest()")
        manifestConfig = config
    }

    init {
        log.lifecycle("> Plug :hytale(settings):initialize in :$projectDir")
        wire(this)
        settings.gradle.settingsEvaluated {
            log.lifecycle("> Plug :hytale(settings):settingsEvaluated in :$projectDir")
            settings.include(":$projectDir")
        }
        settings.gradle.projectsLoaded {
            log.lifecycle("> Plug :hytale(settings):projectsLoaded in :$projectDir")
            val project = configureProject()
            manifestConfig?.let { config ->
                HytaleManifest.from(project).apply(config).configure(project)
                HytaleManifest.from(project).saveTo(project)
            }
        }
    }

    private fun configureProject(): Project {
        log.lifecycle("> Plug :hytale(settings):configureProject($projectDir) in :$projectDir")
        settings.rootDir.resolve(projectDir).mkdirs()
        val project = if (projectDir.isBlank()) {
            settings.gradle.rootProject
        } else {
            settings.gradle.rootProject.project(":$projectDir")
        }
        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project)
        with(TestingEngine::class).configure(project)
        with(HytaleServerPlatform::class).configure(project)
        with(HytaleDevserverRun::class).configure(project)
        return project
    }
}