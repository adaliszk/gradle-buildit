package dev.scaffoldit.gradle.hytale

import dev.buildit.gradle.hytale.HytaleManifest
import dev.scaffoldit.core.ScaffoldIt
import dev.scaffoldit.core.Wired
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.*
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Configure a Hytale project with:
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleProject(private val project: Project) :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Gradle.ConfigurePlatform by HytaleServerPlugin(),
    Gradle.ConfigureIdeaDev by HytaleDevserverRun(),
    Wired by ScaffoldIt() {

    private val log: Logger = Logging.getLogger(this::class.java)

    init {
        wire(this)
        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project)
        with(TestingEngine::class).configure(project)
        with(HytaleServerPlugin::class).configure(project)
        with(HytaleDevserverRun::class).configure(project)
        log.lifecycle("> Plug :hytale(project):initialized")
    }

    fun manifest(config: HytaleManifest.() -> Unit) {
        HytaleManifest.from(project).apply(config).configure(project)
        HytaleManifest.from(project).saveTo(project)
    }
}