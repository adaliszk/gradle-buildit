package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.internal.extensions.core.extra

class ToolchainManager : Gradle.ConfigureToolchain {
    private val log: Logger = Logging.getLogger(ToolchainManager::class.java)
    private val cls: String = "${this::class.simpleName}[${System.identityHashCode(this)}]"

    /**  Kotlin standard library package, use internal or external, your choice. */
    var kotlin: String? = null

    lateinit var project: Project

    /** Use kotlin for this scope, use internal or external, your choice. */
    override fun useKotlin(dependencyNotation: String?) {
        log.lifecycle("> Set $cls:useKotlin($dependencyNotation)")
        kotlin = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        if (::project.isInitialized) configure(project)
    }

    fun configure(project: Project): ToolchainManager {
        log.lifecycle("> Plug :${project.name}:configure(project) by $cls")
        project.extra.set("kotlin", kotlin)
        this.project = project

        with(project.repositories) {
            mavenCentral()
            mavenLocal()
        }

        with(project.pluginManager) {
            apply("java-library")
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
        }

        if (kotlin === null) return this

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.dependencies.add("implementation", kotlin as String)

        return this
    }
}