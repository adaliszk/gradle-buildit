package dev.scaffoldit.gradle.common

import dev.scaffoldit.core.ScaffoldIt
import dev.scaffoldit.core.Wired
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.ToolchainManager
import dev.scaffoldit.gradle.tasks.NestedProjects
import dev.scaffoldit.gradle.tasks.SourceManager
import dev.scaffoldit.gradle.tasks.TestingEngine
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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