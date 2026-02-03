// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.common

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.NestedProjects
import org.gradle.api.initialization.Settings

/**
 * Common Project will configure:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Nested projects with `include()` support
 */
open class CommonSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(),
    CommonExtension() {

    override var projectDir: String = "common"

    init {
        val rootDir = settings.rootDir.resolve(projectDir)
        log.lifecycle("$pfx:common(settings):initialize in $rootDir with ${NestedProjects.included}")
        with(NestedProjects::class).projectDir = projectDir

        settings.gradle.settingsEvaluated {
            if (!rootDir.exists()) return@settingsEvaluated
            log.lifecycle("$pfx:common(settings):settingsEvaluated with ${NestedProjects.withPrefix(projectDir)}")
            settings.include(":$projectDir")
            NestedProjects.withPrefix(projectDir).forEach { subProjectDir ->
                val (dir, path) = resolveProject(subProjectDir)
                settings.rootDir.resolve(path).mkdirs()
                settings.include(dir)
            }
        }

        settings.gradle.projectsLoaded {
            if (!rootDir.exists()) return@projectsLoaded
            log.lifecycle("$pfx:common(settings):projectsLoaded with ${NestedProjects.withPrefix(projectDir)}")
            if (NestedProjects.withPrefix(projectDir).isEmpty()) {
                configureProject(settings.gradle.rootProject.project(projectDir))
                return@projectsLoaded
            }
            NestedProjects.withPrefix(projectDir).forEach { subProjectDir ->
                val (dir, path) = resolveProject(subProjectDir)
                configureProject(settings.gradle.rootProject.project(dir))
            }
        }
    }
}