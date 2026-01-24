package dev.scaffoldit.common

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Common Project will configure:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Nested projects with `include()` support
 */
open class CommonProject(project: Project) :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = "common"

    init {
        wire(this)
        project.afterEvaluate {
            val commonDir = project.rootDir.resolve(projectDir)
            if (commonDir.isDirectory) {
                with(ToolchainManager::class).configure(project)
                with(SourceManager::class).configure(project)
                with(TestingEngine::class).configure(project)
                log.lifecycle("> Plug :common:configure()")
            }
        }
    }
}