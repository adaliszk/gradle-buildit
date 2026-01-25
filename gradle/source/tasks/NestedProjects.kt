package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class NestedProjects(private val settings: Settings) : Gradle.ConfigurePackages {
    private val projects: MutableList<String> = mutableListOf()

    /**
     * Include multiple packages to configure their shared dependencies via
     * opening the include block:
     * ```kotlin
     * include("api", "configs", "commands") {
     *   dependencies {
     *     compileOnly("...")
     *   }
     * }
     * ```
     */
    override fun include(vararg projectPaths: String, userConfig: Project.() -> Unit) {
        projectPaths.forEach { include(it, userConfig) }
    }

    /**
     * Include a package to configure their shared dependencies via
     * opening the include block:
     * ```kotlin
     * include("api") {
     *   dependencies {
     *     compileOnly("...")
     *   }
     * }
     * ```
     */
    override fun include(projectPath: String, userConfig: Project.() -> Unit) {
        val path = ":common:$projectPath".trim(':').replace(':', '/')
        settings.rootDir.resolve(path).mkdirs()
        settings.include(":common:$projectPath".trimEnd(':'))
        projects.add(projectPath.trimEnd(':'))
    }
}