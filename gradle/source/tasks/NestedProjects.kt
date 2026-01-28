// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import groovy.lang.Closure
import org.gradle.api.Project

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging as GradleLogger


class NestedProjects : Gradle.ConfigurePackages {
    private val log: Logger = GradleLogger.getLogger(this::class.java)

    companion object {
        val included: MutableList<String> = mutableListOf()
    }

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

    override fun include(vararg projectList: String, userConfig: Closure<*>) {
        projectList.forEach { include(it, userConfig) }
    }

    override fun include(vararg projectList: String) {
        projectList.forEach { include(it) }
    }

    override fun include(project: String, userConfig: Project.() -> Unit) {
        val projectPath = ":$projectDir:$project".trimEnd(':').replace("::", ":")
        log.lifecycle("NestedProjects.include($projectPath)")
        if (projectPath.trim(':').isBlank()) return
        included.add(projectPath)
    }

    override fun include(project: String) = include(project) {}

    override fun include(project: String, userConfig: Closure<*>) {
        include(project) {
            userConfig.delegate = this
            userConfig.resolveStrategy = Closure.DELEGATE_FIRST
            userConfig.call()
        }
    }
}