package dev.buildit.gradle

import dev.buildit.gradle.common.CommonExtension
import dev.buildit.gradle.hytale.HytaleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware

/**
 * BuildIt! Gradle Plugin
 *
 * Provides minimal boilerplate configuration by automatically configuring basic things such as
 * - Java and Kotlin toolchains
 * - Common and Hytale workspaces
 * - Shared dependencies and repositories
 * - Unit and integration testing
 * - Publishing pipelines
 */
@Suppress("unused") // Used by Gradle, but that is not visible
open class GradlePlugin : Plugin<PluginAware> {
    companion object {
        protected val extensions = mapOf(
            "buildit" to BuildItExtension::class.java,
            "common" to CommonExtension::class.java,
            "hytale" to HytaleExtension::class.java,
        )
    }

    override fun apply(target: PluginAware) {
        when (target) {
            is Settings -> apply(target)
            is Project -> apply(target)
            else -> error(
                "BuildIt can only be applied to Settings or Project"
            )
        }
    }

    protected open fun apply(settings: Settings) {
        val pendingProject = lazy { settings.gradle.rootProject }
        val pendingExtensions = extensions.mapValues { (name, type) ->
            settings.extensions.create(name, type, pendingProject)
        }
        settings.gradle.rootProject {
            pendingExtensions.values.forEach(GradleExtension::initialize)
        }
    }

    protected open fun apply(project: Project) {
        val pendingProject = lazy { project }

        extensions.forEach { (name, type) ->
            project.extensions.create(name, type, pendingProject)
        }
    }
}