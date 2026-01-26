// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.extensions.core.extra
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ToolchainManager : Gradle.ConfigureToolchain {
    private val log: Logger = Logging.getLogger(this::class.java)
    private val cls: String = "${this::class.simpleName}[${System.identityHashCode(this)}]"

    // region Extension APIs

    /**  Kotlin standard library package, use internal or external, your choice. */
    var kotlin: String? = null

    /** Use kotlin for this scope, use internal or external, your choice. */
    override fun useKotlin(dependencyNotation: String?) {
        log.lifecycle("> Set $cls:useKotlin($dependencyNotation)")
        kotlin = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        configure()
    }

    private var pendingRepositoryChanges: (RepositoryHandler.() -> Unit)? = null

    override fun repositories(action: RepositoryHandler.() -> Unit) {
        pendingRepositoryChanges = action
        configure()
    }

    private var pendingDependencyChanges: (DependencyHandler.() -> Unit)? = null

    override fun dependencies(action: DependencyHandler.() -> Unit) {
        pendingDependencyChanges = action
        configure()
    }

    // endregion

    private lateinit var project: Project

    fun configure() {
        if (::project.isInitialized) configure(project)
    }

    fun configure(project: Project): ToolchainManager {
        log.lifecycle("> Plug :${project.name}:configure(project) by $cls")
        this.project = project

        configurePlugins()
        configureRepositories()
        includeWorkspacePackages()
        pendingRepositoryChanges?.let { project.repositories.apply(it) }
        pendingDependencyChanges?.let { project.dependencies.apply(it) }
        configureToolchain()
        configureKotlin()

        return this
    }

    private fun configureRepositories() {
        with(project.repositories) {
            mavenLocal()
            mavenCentral()
            maven { repo ->
                repo.url = project.uri("https://cursemaven.com")
                repo.name = "curse.maven"
            }
        }
    }

    private fun configurePlugins() {
        with(project.pluginManager) {
            apply("java-library")
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
        }
    }

    private fun includeWorkspacePackages() {
        if (!project.gradle.extraProperties.has("includes")) return
        @Suppress("UNCHECKED_CAST") // This is correct; I don't know where to cast it
        val deps = project.gradle.extraProperties.get("includes") as List<String>
        with(project.dependencies) {
            deps.forEach {
                add("implementation", project.project(it))
            }
        }
    }

    private fun configureToolchain() {
        with(project.extensions) {
            configure(JavaPluginExtension::class.java) { ext ->
                ext.toolchain.languageVersion.set(JavaLanguageVersion.of(25)) // TODO: Expose the version as a configuration
                ext.withSourcesJar()
                ext.withJavadocJar()
            }
        }

        project.tasks.withType(JavaCompile::class.java).configureEach {
            it.options.release.set(21)  // TODO: Expose the version as a configuration
        }
    }

    private fun configureKotlin() {
        if (kotlin === null) return

        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
        }

        with(project.dependencies) {
            add("implementation", kotlin as String)
        }

        project.extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            it.jvmToolchain(25)  // TODO: Expose the version as a configuration
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.compilerOptions.jvmTarget.set(JvmTarget.JVM_24) // TODO: Expose the version as a configuration
        }

        project.extraProperties.set("kotlin", kotlin)
    }
}