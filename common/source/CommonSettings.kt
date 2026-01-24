package dev.scaffoldit.common

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Common Project will configure:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Nested projects with `include()` support
 */
open class CommonSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(settings),
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Wired by ScaffoldIt() {

    // TODO: Figure out how to use the nested projects

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = "common"

    init {
        wire(this)
        log.lifecycle("> Plug :common:initialize()")
        include(":$projectDir")
        settings.gradle.projectsLoaded { gradle ->
            val project = gradle.rootProject.project(":$projectDir")
            with(ToolchainManager::class).configure(project)
            with(SourceManager::class).configure(project)
            log.lifecycle("> Plug :common:initialize():done")
        }
    }
}