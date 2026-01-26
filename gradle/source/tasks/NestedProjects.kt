// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class NestedProjects(private val settings: Settings) : Gradle.ConfigurePackages {
    override val _projectList: MutableList<String> = mutableListOf()

    override var projectDir: String = ""

    /**
     * Include multiple packages to configure their shared dependencies via
     * opening the include block:
     * ```kotlin
     * include("api", "configs", "commands") {
     *   dependencies {
     *     compileOnly("...")
     *   }
     * }
     * ```
     */
    override fun include(vararg projectList: String, userConfig: Project.() -> Unit) {
        projectList.forEach { include(it, userConfig) }
    }

    /**
     * Include a package to configure their shared dependencies via
     * opening the include block:
     * ```kotlin
     * include("api") {
     *   dependencies {
     *     compileOnly("...")
     *   }
     * }
     * ```
     */
    override fun include(project: String, userConfig: Project.() -> Unit) {
        val projectPath = ":$projectDir:$project".trimEnd(':').replace("::", ":")
        val resolvedPath = projectPath.replace(':', '/')
        if (resolvedPath.isBlank()) return
        settings.gradle.extraProperties.set("monorepo", true)
        settings.rootDir.resolve(resolvedPath).mkdirs()
        settings.include(projectPath)
        _projectList.add(projectPath)
    }
}