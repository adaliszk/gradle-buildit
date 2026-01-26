// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

import dev.scaffoldit.api.Trait
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
    val _projectList: MutableList<String>
    fun include(vararg projectList: String, userConfig: Project.() -> Unit = {})
    fun include(project: String, userConfig: Project.() -> Unit = {})
    var projectDir: String
  }

  interface ConfigureToolchain : Trait {
    val _wiredConfigureToolchain: ConfigureToolchain get() = this
    fun useKotlin(dependencyNotation: String? = null)
    fun repositories(action: RepositoryHandler.() -> Unit)
    fun dependencies(action: DependencyHandler.() -> Unit)
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