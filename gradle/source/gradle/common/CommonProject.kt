package dev.scaffoldit.gradle.common

import dev.scaffoldit.core.ScaffoldIt
import dev.scaffoldit.core.Wired
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.*
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Configures common library projects
 */
open class CommonProject(project: Project) :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)


    init {
        wire(this)
        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project)
        with(TestingEngine::class).configure(project)
        log.lifecycle("> Plug :common:initialize()")
    }
}