package dev.buildit.gradle

import dev.buildit.hytale.HytaleProject
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

@Suppress("unused") // Used by Gradle, but that is not visible
open class SettingsPlugin : Plugin<Settings>
{
    protected open val extensions = mapOf(
        "common" to CommonPresets::class.java,
        "hytale" to HytaleProject::class.java,
    )

    override fun apply(settings: Settings)
    {
        settings.extensions.create("buildit", SettingsExtension::class.java)

        val registeredExtensions = extensions.mapValues {
            settings.extensions.create(it.key, it.value, settings)
        }

        registeredExtensions.values.forEach(GradleExtension::configureSettings)
        settings.gradle.projectsLoaded {
            settings.gradle.rootProject.allprojects.forEach { project ->
                registeredExtensions.values.forEach { extension ->
                    extension.configureProject(project)
                }
            }
        }
    }
}