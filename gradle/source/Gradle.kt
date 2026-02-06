// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

import dev.scaffoldit.api.Trait
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler

@Suppress("PropertyName", "unused") // These are internal ones used for wiring!
object Gradle {
    interface ConfigurePaths : Trait {
        val _wiredConfigurePaths: ConfigurePaths get() = this
        var projectDir: String
        fun useFlat()
    }

    interface ConfigurePackages : Trait {
        val _wiredConfigurePackages: ConfigurePackages get() = this
        fun include(vararg projectList: String, userConfig: Project.() -> Unit = {})
        fun include(vararg projectList: String, userConfig: Closure<*>)
        fun include(vararg projectList: String)
        fun include(project: String, userConfig: Project.() -> Unit = {})
        fun include(project: String, userConfig: Closure<*>)
        fun include(project: String)
        var projectDir: String
    }

    class ToolchainDependencyHandler(private val project: Project) {
        fun implementation(dependency: String) = project.dependencies.add("implementation", dependency)
        fun compileOnly(dependency: String) = project.dependencies.add("compileOnly", dependency)
        fun runtimeOnly(dependency: String) = project.dependencies.add("runtimeOnly", dependency)
        fun api(dependency: String) = project.dependencies.add("api", dependency)
        fun annotationProcessor(dependency: String) = project.dependencies.add("annotationProcessor", dependency)
        // Compatibility for a previously exposed solution:
        fun add(type: String, dependency: String) = project.dependencies.add(type, dependency)
    }

    interface ConfigureToolchain : Trait {
        val _wiredConfigureToolchain: ConfigureToolchain get() = this
        fun useKotlin(dependencyNotation: String? = null)
        val kotlin: String?
        fun repositories(action: RepositoryHandler.() -> Unit)
        fun repositories(action: Action<RepositoryHandler>)
        fun dependencies(action: ToolchainDependencyHandler.() -> Unit)
        fun dependencies(action: Action<ToolchainDependencyHandler>)
    }

    interface ConfigureTests : Trait {
        val _wiredConfigureTests: ConfigureTests get() = this
    }

    interface ConfigurePlatform : Trait {
        val _wiredConfigurePlatform: ConfigurePlatform get() = this
    }

    interface ConfigureIdeaDev : Trait {
        val _wiredConfigureIdeaDev: ConfigureIdeaDev get() = this
        var devserverDir: String
    }

    interface ConfigureIdeaBuild : Trait {
        val _wiredConfigureIdeaBuild: ConfigureIdeaBuild get() = this
    }

    interface ConfigureIdeaEnv : Trait {
        val _wiredConfigureIdeaEnv: ConfigureIdeaEnv get() = this
    }

    interface ConfigureVscodeDev : Trait {
        val _wiredConfigureVscodeDev: ConfigureVscodeDev get() = this
    }

    interface ConfigureVscodeBuild : Trait {
        val _wiredConfigureVscodeBuild: ConfigureVscodeBuild get() = this
    }
}