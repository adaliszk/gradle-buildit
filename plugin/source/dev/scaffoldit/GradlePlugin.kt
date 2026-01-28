package dev.scaffoldit

import dev.scaffoldit.common.CommonProject
import dev.scaffoldit.common.CommonSettings
import dev.scaffoldit.gradle.ExtensionSet
import dev.scaffoldit.gradle.Plugin
import dev.scaffoldit.hytale.HytaleProject
import dev.scaffoldit.hytale.HytaleSettings

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
open class GradlePlugin : Plugin() {
    override val extensions = mapOf(
        "common" to ExtensionSet(CommonSettings::class.java, CommonProject::class.java),
        "hytale" to ExtensionSet(HytaleSettings::class.java, HytaleProject::class.java),
    )
}