package dev.scaffoldit.gradle

import dev.scaffoldit.core.Trait
import org.gradle.api.Project

@Suppress("PropertyName", "unused") // These are internal ones used for wiring!
object Gradle {
    interface ConfigurePaths : Trait {
        val _wiredConfigurePaths: ConfigurePaths get() = this
        var projectDir: String
        fun useFlat()
    }

    interface ConfigurePackages : Trait {
        val _wiredConfigurePackages: ConfigurePackages get() = this
        fun include(vararg projectPaths: String, userConfig: Project.() -> Unit = {})
        fun include(projectPath: String, userConfig: Project.() -> Unit = {})
    }

    interface ConfigureToolchain : Trait {
        val _wiredConfigureToolchain: ConfigureToolchain get() = this
        fun useKotlin(dependencyNotation: String? = null)
    }

    interface ConfigureTests : Trait {
        val _wiredConfigureTests: ConfigureTests get() = this
    }

    interface ConfigurePlatform : Trait {
        val _wiredConfigurePlatform: ConfigurePlatform get() = this
    }

    interface ConfigureIdeaDev : Trait {
        val _wiredConfigureIdeaDev: ConfigureIdeaDev get() = this
    }

    interface ConfigureIdeaBuild : Trait {
        val _wiredConfigureIdeaBuild: ConfigureIdeaBuild get() = this
    }
}