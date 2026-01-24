package dev.scaffoldit.gradle.hytale

import dev.buildit.gradle.hytale.HytaleManifest
import dev.scaffoldit.core.ScaffoldIt
import dev.scaffoldit.core.Wired
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.tasks.HytaleServerPlugin
import dev.scaffoldit.gradle.tasks.ToolchainManager
import dev.scaffoldit.gradle.tasks.NestedProjects
import dev.scaffoldit.gradle.tasks.SourceManager
import dev.scaffoldit.gradle.tasks.TestingEngine
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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