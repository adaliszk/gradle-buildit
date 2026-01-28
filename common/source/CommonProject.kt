// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.common

import dev.scaffoldit.gradle.tasks.*
import org.gradle.api.Project

/**
 * Common Project will configure:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Nested projects with `include()` support
 */
open class CommonProject(private val project: Project) : CommonExtension() {
    init {
        val rootDir = project.rootDir.resolve(projectDir)
        log.lifecycle("$pfx:common(project):initialize with ${rootDir.canonicalPath}")
        with(ToolchainManager::class).project = project
        project.afterEvaluate {
            if (!rootDir.exists()) return@afterEvaluate
            if (rootDir != project.rootDir) return@afterEvaluate
            log.lifecycle("$pfx:common(project):afterEvaluate with ${NestedProjects.included}")
            configureProject(project)
        }
    }
}