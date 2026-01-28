// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.*
import org.gradle.api.initialization.Settings

/**
 * Configure a Hytale project with:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(),
    HytaleExtension() {

    override var projectDir: String = "hytale"

    init {
        with(NestedProjects::class).projectDir = projectDir

        settings.gradle.settingsEvaluated {
            log.lifecycle("$pfx:hytale(settings):settingsEvaluated with ${NestedProjects.included}")
            when (true) {
                NestedProjects.included.isEmpty() -> projectDir = ""
                else -> NestedProjects.included.forEach { subProjectDir ->
                    val (dir, path) = resolveProjectPath(subProjectDir)
                    val targetPath = settings.rootDir.resolve(path).also { it.mkdirs() }
                    settings.include(dir)
                }
            }
        }

        settings.gradle.projectsLoaded {
            log.lifecycle("$pfx:hytale(settings):projectsLoaded with ${NestedProjects.included}")
            when (true) {
                NestedProjects.included.isEmpty() -> configureRootProject(settings.gradle.rootProject)
                else -> NestedProjects.included.forEach { subProjectDir ->
                    val (dir, _) = resolveProjectPath(subProjectDir)
                    configureProject(settings.gradle.rootProject.project(dir))
                }
            }
        }
    }
}