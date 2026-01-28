package dev.scaffoldit.hytale

import dev.scaffoldit.api.Logging
import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.Extension
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.GradleConsole
import dev.scaffoldit.gradle.tasks.SourceManager
import dev.scaffoldit.gradle.tasks.TestingEngine
import dev.scaffoldit.gradle.tasks.ToolchainManager
import org.gradle.api.Project
import java.io.File
import org.gradle.api.logging.Logger as InGradle

abstract class HytaleExtension :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Gradle.ConfigurePlatform by HytaleServerPlatform(),
    Gradle.ConfigureIdeaDev by HytaleDevserverRun(),
    Logging<InGradle> by GradleConsole(),
    Wired by ScaffoldIt(),
    Extension {

    internal val pfx: String = "> Plug "

    override var projectDir: String = "hytale"

    init {
        wire(this)
    }

    internal var pendingManifest: (HytaleManifest.() -> Unit)? = null

    @Suppress("unused") // Exposed for Gradle scripts
    fun manifest(config: HytaleManifest.() -> Unit) {
        log.lifecycle("$pfx:$projectDir:manifest()")
        pendingManifest = config
    }

    internal fun resolveProjectPath(subDir: String): Pair<String, String> {
        val dir = ":$subDir".replace("::", ":")
        val path = dir.trim(':').replace(':', '/')
        return dir to path
    }

    internal fun configureRootProject(project: Project) {
        projectDir = ""
        configureProject(project)
    }

    internal fun configureProject(project: Project) {
        log.lifecycle("$pfx:hytale:configureProject(${project.name})")

        with(SourceManager::class).projectDir = projectDir
        with(ToolchainManager::class).configure(project)
        with(SourceManager::class)
            .withKotlin(with(ToolchainManager::class).kotlin)
            .configure(project)
        with(TestingEngine::class).configure(project)
        with(HytaleServerPlatform::class).configure(project)
        with(HytaleDevserverRun::class).configure(project)

        pendingManifest?.let { config ->
            HytaleManifest.from(project).apply(config).configure(project)
            HytaleManifest.from(project).saveTo(project)
        }
    }
}
