// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class NestedProjects(private val settings: Settings) : Gradle.ConfigurePackages {
    private val log: Logger = Logging.getLogger(this::class.java)

    override val _projectList: MutableList<String> = mutableListOf()

    override var projectDir = ""

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
        log.lifecycle("> Plug :nested(settings):include($project) in :$projectDir")
        val projectPath = ":$projectDir:$project".trimEnd(':').replace("::", ":")
        val resolvedPath = projectPath.trimStart(':').replace(':', '/')
        if (resolvedPath.isBlank()) return
        settings.gradle.extraProperties.set("monorepo", true)
        settings.rootDir.resolve(resolvedPath).mkdirs()
        settings.include(projectPath)
        _projectList.add(projectPath)
        settings.gradle.extraProperties.set("includes", _projectList)
    }
}