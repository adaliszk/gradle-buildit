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
    Gradle.ConfigurePlatform by HytaleServerPlatform(),
    Gradle.ConfigureIdeaDev by HytaleDevserverRun(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    override var projectDir: String = ""

    init {
        wire(this)
        log.lifecycle("> Plug :hytale(settings):initialize()")
        settings.gradle.projectsLoaded { gradle ->
            val project = gradle.rootProject.project(":$projectDir")
            with(ToolchainManager::class).configure(project)
            with(SourceManager::class).configure(project)
            with(TestingEngine::class).configure(project)
            with(HytaleServerPlatform::class).configure(project)
            with(HytaleDevserverRun::class).configure(project)
            log.lifecycle("> Plug :hytale(settings):initialize():projectsLoaded")
        }
    }

    fun manifest(config: HytaleManifest.() -> Unit) {
        log.lifecycle("> Plug :hytale(settings):manifest()")
        settings.gradle.projectsLoaded { gradle ->
            val project = gradle.rootProject.project(":$projectDir")
            HytaleManifest.from(project).apply(config).configure(project)
            HytaleManifest.from(project).saveTo(project)
            log.lifecycle("> Plug :hytale(settings):manifest():projectsLoaded")
        }
    }
}