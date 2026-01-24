package dev.scaffoldit.gradle.tasks

import dev.buildit.gradle.hytale.HytaleManifest
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.hytale.HytaleConfig
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy

class HytaleServerPlugin : Gradle.ConfigurePlatform {
    private val log: Logger = Logging.getLogger(this::class.java)

    fun configure(project: Project): Gradle.ConfigurePlatform {
        log.lifecycle("> Plug :${project.name}:HytaleServerPlugin(project)")

        val config = HytaleConfig(project)
        config.validateHytaleInstallation()

        with(project.dependencies) {
            add("implementation", project.files(config.serverFilePath))
        }

        with(project.tasks) {
            val updateManifest =
                findByName("updateHytaleManifest") ?: register("updateHytaleManifest") { task ->
                    task.doFirst {
                        HytaleManifest.from(project).saveTo(project)
                    }
                }

            project.afterEvaluate {
                named("processResources", Copy::class.java) { task ->
                    task.dependsOn(updateManifest)
                    task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
                }
            }
        }

        return this
    }
}