// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware

/**
 * ScaffoldIt! Gradle Plugin Base
 * It registers extensions, so you can declare your own:
 * ```kotlin
 * open class CustomPlugin : Plugin() {
 *     override val extension = mapOf(
 *         "custom" to Extension(
 *             CustomSettings::class.java,
 *             CustomProject::class.java,
 *         ),
 *     )
 * }
 * ```
 */
@Suppress("unused") // Used by Gradle, but that is not visible
open class Plugin : Plugin<PluginAware> {
    private val log: Logger = Logging.getLogger(Plugin::class.java)

    open val extensions: Map<String, Extension> = mapOf(
        // "common" to Extension(CommonSettings::class.java, CommonProject::class.java),
        // "hytale" to Extension(HytaleSettings::class.java, HytaleProject::class.java),
    )

    override fun apply(target: PluginAware) {
        when (target) {
            is Settings -> apply(target)
            is Project -> apply(target)
            else -> error(
                "ScaffoldIt can only be applied to Settings or Project"
            )
        }
    }

    open fun apply(settings: Settings) {
        log.lifecycle("> Plug :${this::class.simpleName}.apply(settings)")
        extensions.mapValues { (name, type) ->
            settings.extensions.create(name, type.setting, settings)
        }
    }

    open fun apply(project: Project) {
        log.lifecycle("> Plug :${this::class.simpleName}.apply(project)")
        extensions.forEach { (name, type) ->
            project.extensions.create(name, type.project, project)
        }
    }
}