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
import dev.scaffoldit.hytale.wire.HytaleGradle
import dev.scaffoldit.hytale.wire.HytaleManifest
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.logging.Logger as InGradle

abstract class HytaleExtension :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    HytaleGradle.ConfigurePlatform by HytaleServerPlatform(),
    HytaleGradle.ConfigureIdeaDev by HytaleDevServerRun(),
    Logging<InGradle> by GradleConsole(),
    Wired by ScaffoldIt(),
    Extension {

    companion object {
        // Configuration for Hytale
        var patchline: String = "release"
        var version: String = "+"
    }

    internal val pfx: String = "> Plug "

    override var projectDir: String = "hytale"

    init {
        wire(this)
    }

    internal fun resolveProjectPath(subDir: String): Pair<String, String> {
        val dir = ":$subDir".replace("::", ":")
        val path = dir.trim(':').replace(':', '/')
        return dir to path
    }

    internal fun configureProject(project: Project) {
        log.lifecycle("$pfx:hytale:configureProject(${project.name})")

        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project, parent)
        with(TestingEngine::class).configure(project)

        // TODO: Figure out how not to cast here
        with(HytaleServerPlatform::class).configure(
            project, parent as HytaleGradle.ConfigurePlatform
        )
        with(HytaleDevServerRun::class).configure(
            project, parent as Gradle.ConfigureToolchain
        )
    }
}
