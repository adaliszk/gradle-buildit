// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit

import dev.scaffoldit.common.CommonProject
import dev.scaffoldit.common.CommonSettings
import dev.scaffoldit.gradle.Extension
import dev.scaffoldit.gradle.ExtensionSet
import dev.scaffoldit.gradle.tasks.NestedProjects
import dev.scaffoldit.hytale.HytaleProject
import dev.scaffoldit.hytale.HytaleSettings
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.ProviderFactory

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
 *
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
open class GradlePlugin : Plugin<PluginAware> {
    private val log: Logger = Logging.getLogger(Plugin::class.java)

    val extensions = mapOf(
        "common" to ExtensionSet(
            CommonSettings::class.java,
            CommonProject::class.java,
            "env.scaffoldit.monorepoSupport"
        ),
        "hytale" to ExtensionSet(
            HytaleSettings::class.java,
            HytaleProject::class.java,
        ),
    )

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
        settings.pluginManagement.repositories {
            it.gradlePluginPortal()
            it.mavenLocal()
            it.mavenCentral()
        }
        extensions
            .filter { (_, extension) -> isFeatureEnabled(settings.providers, extension) }
            .mapValues { (name, type) ->
                settings.extensions.create(name, type.setting, settings)
            }
    }

    open fun apply(project: Project) {
        extensions
            .filter { (_, extension) -> isFeatureEnabled(project.providers, extension) }
            .mapValues { (name, type) ->
                project.extensions.create(name, type.project, project)
            }
    }

    private fun <A : Extension, B : Extension> isFeatureEnabled(
        providers: ProviderFactory,
        extension: ExtensionSet<A, B>
    ): Boolean {
        if (extension.featureFlag == null) return true

        return providers.gradleProperty(extension.featureFlag as String)
            .getOrElse("true")
            .toBoolean()
    }
}