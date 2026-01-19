package dev.buildit.gradle.common

import dev.buildit.gradle.GradleExtension
import org.gradle.api.Project

open class CommonExtension(pending: Lazy<Project>) : GradleExtension(pending) {

    fun include(vararg projectPaths: String, userConfig: Project.() -> Unit = {}) {
        projectPaths.forEach { include(it, userConfig) }
    }

    fun include(projectPath: String, userConfig: Project.() -> Unit) {
        val resolvedPath = ":common:$projectPath"
        register(resolvedPath, userConfig)
        libs.add(resolvedPath)
    }

    fun useKotlin(dependencyNotation: String? = null) {
        TODO("Not implemented yet")
    }
}