package dev.scaffoldit.hytale

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Configure a Hytale project with:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleSettings(protected val settings: Settings) :
    Gradle.ConfigurePackages by NestedProjects(settings),
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Gradle.ConfigurePlatform by HytaleServerPlugin(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = "common"

    init {
        wire(this)
        log.lifecycle("> Plug :hytale(settings):initialize()")
        include(":$projectDir")
        settings.gradle.projectsLoaded { gradle ->
            val project = gradle.rootProject.project(":$projectDir")
            with(ToolchainManager::class).configure(project)
            with(SourceManager::class).configure(project)
            with(TestingEngine::class).configure(project)
            with(HytaleServerPlugin::class).configure(project)
            log.lifecycle("> Plug :hytale(settings):initialize():done")
        }
    }

    fun manifest(config: HytaleManifest.() -> Unit) {
        settings.gradle.projectsLoaded { gradle ->
            val project = gradle.rootProject.project(":$projectDir")
            HytaleManifest.from(project).apply(config).configure(project)
            HytaleManifest.from(project).saveTo(project)
        }
    }
}