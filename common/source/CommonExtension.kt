package dev.scaffoldit.common

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
import org.gradle.api.logging.Logger as InGradle

abstract class CommonExtension :
    Gradle.ConfigureToolchain by ToolchainManager(),
    Gradle.ConfigurePaths by SourceManager(),
    Gradle.ConfigureTests by TestingEngine(),
    Logging<InGradle> by GradleConsole(),
    Wired by ScaffoldIt(),
    Extension {

    internal val pfx: String = "> Plug "

    override var projectDir: String = "common"

    init {
        wire(this)
    }

    internal fun resolveProject(subDir: String): Pair<String, String> {
        val dir = subDir.replace("::", ":")
        val path = dir.trim(':').replace(':', '/')
        return dir to path
    }

    internal fun configureProject(project: Project) {
        log.lifecycle("$pfx:common:configureProject(${project.name})")

        with(ToolchainManager::class).configure(project)
        with(SourceManager::class).configure(project, parent)
        with(TestingEngine::class).configure(project)
    }
}
