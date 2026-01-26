// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.common

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.internal.extensions.core.extra

/**
 * Common Project will configure:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Nested projects with `include()` support
 */
open class CommonSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(settings),
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = "common"

    init {
        wire(this)
        log.lifecycle("> Plug :common(settings):initialize in :$projectDir")
        if (settings.rootDir.resolve(projectDir).exists()) {
            settings.include(":$projectDir")
        }
        settings.gradle.projectsLoaded { gradle ->
            log.lifecycle("> Plug :common(settings):projectsLoaded in :$projectDir")
            val project = gradle.rootProject.project(":$projectDir")
            with(NestedProjects::class) {
                if (projectDir.isBlank() && _projectList.isEmpty()) {
                    configureProject(project)
                }
                _projectList.forEach { path ->
                    val project = gradle.rootProject.project(path)
                    configureProject(project)
                }
            }
        }
    }

    private fun configureProject(project: Project) {
        log.lifecycle("> Plug :common(settings):configureProject($projectDir)")
        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project)
        with(TestingEngine::class).configure(project)
    }
}