package dev.buildit.gradle

import dev.buildit.gradle.extensions.CommonExtension
import dev.buildit.gradle.extensions.BuilditExtension
import dev.buildit.gradle.hytale.HytaleProject
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

@Suppress("unused") // Used by Gradle, but that is not visible
open class SettingsPlugin : Plugin<Settings>
{
    protected open val extensions = mapOf(
        "common" to CommonExtension::class.java,
        "hytale" to HytaleProject::class.java,
    )

    override fun apply(settings: Settings)
    {
        settings.extensions.create("buildit", BuilditExtension::class.java)

        val registeredExtensions = extensions.mapValues {
            settings.extensions.create(it.key, it.value, settings)
        }

        with(settings.gradle) {
            projectsLoaded {
                registeredExtensions.values
                    .flatMap { extension -> rootProject.allprojects.map { extension to it } }
                    .forEach { (extension, project) -> extension.configureProject(project) }
            }
        }
    }
}