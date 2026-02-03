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
        log.debug("ToolchainManager.useKotlin($kotlin)")
        if (::project.isInitialized) configureKotlin()
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
        val autoToolchain = project.providers
            .gradleProperty("env.scaffoldit.autoToolchain")
            .getOrElse("true").toBoolean()
        if (!autoToolchain) return this

        log.debug("ToolchainManager.configure(kotlin=$kotlin)")
        this.project = project

        configurePlugins()
        configureRepositories()
        configureToolchain()
        configureKotlin()
        includeWorkspacePackages()
        pendingRepositoryChanges?.let { project.repositories.apply(it) }
        pendingDependencyChanges?.let { project.dependencies.apply(it) }

        return this
    }

    // endregion

    private fun configureRepositories() {
        with(project.repositories) {
            mavenLocal()
            mavenCentral()
            maven { repo ->
                repo.url = project.uri("https://cursemaven.com")
                repo.name = "curse"
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

    private fun configureToolchain() {
        with(project.extensions) {
            configure(JavaPluginExtension::class.java) { ext ->
                ext.toolchain.languageVersion.set(
                    JavaLanguageVersion.of(
                        project.providers
                            .gradleProperty("env.java.version")
                            .getOrElse("25")
                            .toInt()
                    )
                )

                if(project.providers
                    .gradleProperty("env.java.compileSources")
                    .getOrElse("true").toBoolean()) {
                    ext.withSourcesJar()
                }

                if(project.providers
                        .gradleProperty("env.java.compileDocs")
                        .getOrElse("false").toBoolean()) {
                    ext.withJavadocJar()
                }
            }
        }

        project.tasks.withType(JavaCompile::class.java).configureEach {
            it.options.release.set(
                project.providers
                    .gradleProperty("env.java.version")
                    .getOrElse("25")
                    .toInt()
            )
        }
    }

    private fun configureKotlin() {
        val kotlinSupport = project.providers
            .gradleProperty("env.scaffoldit.kotlinSupport")
            .getOrElse("true").toBoolean()
        if (!kotlinSupport || kotlin === null) return

        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
        }

        with(project.dependencies) {
            add("implementation", kotlin as String)
        }

        project.afterEvaluate {
            project.extensions.configure<KotlinJvmProjectExtension>("kotlin") {
                it.jvmToolchain(
                    project.providers
                        .gradleProperty("env.java.version")
                        .getOrElse("25")
                        .toInt()
                )
            }
            project.tasks.withType(KotlinCompile::class.java).configureEach {
                val version = project.providers
                    .gradleProperty("env.java.version")
                    .getOrElse("25")
                    .toInt()
                it.compilerOptions.jvmTarget.set(JvmTarget.valueOf("JVM_$version"))
            }
        }
    }

    private fun includeWorkspacePackages() {
        val monorepoAutoDepends = project.providers
            .gradleProperty("env.scaffoldit.monorepoAutoDepends")
            .getOrElse("true").toBoolean()
        if (!monorepoAutoDepends) return

        with(project.dependencies) {
            NestedProjects.included.forEach {
                add("implementation", project.project(it))
            }
        }
    }
}