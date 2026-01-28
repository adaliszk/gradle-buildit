// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.api.VERSION
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy

class HytaleServerPlatform : Gradle.ConfigurePlatform {
    fun configure(project: Project): Gradle.ConfigurePlatform {
        val config = HytaleConfig(project)

        project.repositories.maven {
            it.url = project.uri("https://maven.hytale.com/${config.patchline}")
        }

        with(project.dependencies) {
            add("compileOnly", "com.hypixel.hytale:Server:+")
            add("runtimeOnly", "com.hypixel.hytale:Server:+")
            add(
                "runtimeOnly",
                "dev.scaffoldit:devtools:${VERSION}"
            )
            project.gradle.allprojects { lib ->
                add("implementation", lib)
            }
        }

        with(project.tasks) {
            val updateManifest =
                findByName("updateHytaleManifest") ?: register("updateHytaleManifest") { task ->
                    task.doFirst {
                        HytaleManifest.from(project).saveTo(project)
                    }
                }

            named("processResources", Copy::class.java) { task ->
                task.dependsOn(updateManifest)
                task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }

        return this
    }
}