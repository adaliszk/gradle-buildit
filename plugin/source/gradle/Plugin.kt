package dev.scaffoldit.gradle

import dev.scaffoldit.gradle.common.*
import dev.scaffoldit.gradle.hytale.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

/**
 * ScaffoldIt! Gradle Plugin
 *
 * Provides minimal boilerplate configuration by automatically configuring basic things such as
 * - Java and Kotlin toolchains
 * - Common, Hytale, and later NeoForge/Fabric workspaces
 * - Annotations to wire cross-platform structures into mods
 * - Auto-scaffolding for components and projects
 * - Shared dependencies and repositories
 * - Unit and integration testing
 * - Publishing pipelines
 */
@Suppress("unused") // Used by Gradle, but that is not visible
open class Plugin : Plugin<PluginAware> {
    private val log: Logger = Logging.getLogger(Plugin::class.java)

    internal data class Extension(
        val setting: Class<*>,
        val project: Class<*>
    )

    companion object {
        internal val extensions: Map<String, Extension> = mapOf(
            "common" to Extension(CommonSettings::class.java, CommonProject::class.java),
            "hytale" to Extension(HytaleSettings::class.java, HytaleProject::class.java),
        )
    }

    override fun apply(target: PluginAware) {
        when (target) {
            is Settings -> apply(target)
            is Project -> apply(target)
            else -> error(
                "ScaffoldIt can only be applied to Settings or Project"
            )
        }
    }

    internal fun apply(settings: Settings) {
        log.lifecycle("> Plug :${this::class.simpleName}.apply(settings)")
        extensions.mapValues { (name, type) ->
            settings.extensions.create(name, type.setting, settings)
        }
    }

    internal fun apply(project: Project) {
        log.lifecycle("> Plug :${this::class.simpleName}.apply(project)")
        extensions.forEach { (name, type) ->
            project.extensions.create(name, type.project, project)
        }
    }
}