// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.gradle.tasks.*
import org.gradle.api.Project

/**
 * Configure a Hytale project with:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleProject(private val project: Project) : HytaleExtension() {
    init {
        val rootDir = project.rootDir.resolve(projectDir)
        log.lifecycle("$pfx:hytale(project):initialize with ${rootDir.canonicalPath}")
        with(ToolchainManager::class).project = project
        project.afterEvaluate {
            log.lifecycle("$pfx$projectDir(hy:project):afterEvaluate with ${NestedProjects.included}")
            configureProject(project)
        }
    }
}