@file:Suppress("unused") // The public methods are used by Gradle, but not visible

package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.initialization.Settings


open class CommonPresets(settings: Settings) : CraftableExtension(settings)
{
    fun include(vararg projectPaths: String, userConfig: Project.() -> Unit = {})
    {
        projectPaths.forEach { include(it, userConfig) }
    }

    fun include(projectPath: String, userConfig: Project.() -> Unit)
    {
        val resolvedPath = ":libraries:$projectPath"
        libs.add(resolvedPath)

        register(resolvedPath) {
            pluginManager.apply("java-library")
            userConfig()
        }
    }
}