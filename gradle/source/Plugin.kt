// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

import dev.scaffoldit.gradle.tasks.NestedProjects
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

    open val extensions: Map<String, ExtensionSet<*, *>> = emptyMap()

    override fun apply(target: PluginAware) {
        // There is no real isolation during testing, so we need to reset:
        NestedProjects.included.clear()
        // Real lifetime:
        when (target) {
            is Settings -> apply(target)
            is Project -> apply(target)
            else -> error(
                "ScaffoldIt can only be applied to Settings or Project"
            )
        }
    }

    open fun apply(settings: Settings) {
        extensions.mapValues { (name, type) ->
            settings.extensions.create(name, type.setting, settings)
        }
    }

    open fun apply(project: Project) {
        extensions.mapValues { (name, type) ->
            project.extensions.create(name, type.project, project)
        }
    }
}