// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import org.gradle.api.logging.Logger
import org.gradle.internal.extensions.core.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.gradle.api.logging.Logging as GradleLogger

class ToolchainManager : Gradle.ConfigureToolchain {
    var log: Logger = GradleLogger.getLogger(this::class.java)

    /**  Kotlin standard library package, use internal or external, your choice. */
    override var kotlin: String? = null

    /** Use kotlin for this scope, use internal or external, your choice. */
    override fun useKotlin(dependencyNotation: String?) {
        kotlin = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        log.lifecycle("ToolchainManager.useKotlin($kotlin)")
    }

    private var pendingRepositoryChanges: (RepositoryHandler.() -> Unit)? = null

    override fun repositories(action: RepositoryHandler.() -> Unit) {
        pendingRepositoryChanges = action
    }

    private var pendingDependencyChanges: (DependencyHandler.() -> Unit)? = null

    override fun dependencies(action: DependencyHandler.() -> Unit) {
        pendingDependencyChanges = action
    }

    // region Internal APIs

    lateinit var project: Project

    fun configure() {
        if (::project.isInitialized) configure(project)
    }

    fun configure(project: Project): ToolchainManager {
        log.debug("ToolchainManager.configure(kotlin=$kotlin)")
        project.extra.set("kotlin", kotlin)
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

    // endregion

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
            if (kotlin !== null) {
                apply("org.jetbrains.kotlin.jvm")
            } else {
                apply("java-library")
            }
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
        }
    }

    private fun includeWorkspacePackages() {
        with(project.dependencies) {
            NestedProjects.included.forEach {
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
            it.options.release.set(25)  // TODO: Expose the version as a configuration
        }
    }

    private fun configureKotlin() {
        if (kotlin === null) return

        with(project.dependencies) {
            add("implementation", kotlin as String)
        }

        project.extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            it.jvmToolchain(25)  // TODO: Expose the version as a configuration
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.compilerOptions.jvmTarget.set(JvmTarget.JVM_25) // TODO: Expose the version as a configuration
        }
    }
}