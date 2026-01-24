package dev.scaffoldit.hytale

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy

class HytaleServerPlatform : Gradle.ConfigurePlatform {
    private val log: Logger = Logging.getLogger(this::class.java)

    fun configure(project: Project): Gradle.ConfigurePlatform {
        log.lifecycle("> Plug :${project.name}:HytaleServerPlugin(project)")

        val config = HytaleConfig(project)

        project.repositories.maven {
            it.url = project.uri("https://maven.hytale.com/${config.patchline}")
        }

        with(project.dependencies) {
            add("compileOnly", "com.hypixel.hytale:Server:+")
            add("runtimeOnly", "com.hypixel.hytale:Server:+")
            // add("compileOnly", project.files(config.serverFilePath))
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