// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.extensions.core.extra
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class TestingEngine : Gradle.ConfigureTests {
    fun configure(project: Project): TestingEngine {
        val useKotlin = project.extraProperties.has("kotlin")

        project.pluginManager.apply("org.gradle.jacoco")

        // TODO: Expose the versions for users
        project.dependencies.apply {
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            // TODO: Expose the versions as a configuration
            if (useKotlin) {
                add("testImplementation", "io.kotest:kotest-runner-junit5:5.8.0")
                add("testImplementation", "io.kotest:kotest-assertions-core:5.8.0")
                add("testImplementation", "io.kotest:kotest-property:5.8.0")
            } else {
                add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
                add("testImplementation", "org.assertj:assertj-core:3.24.2")
            }
        }
        project.tasks.named("test", Test::class.java) { test ->
            test.useJUnitPlatform()
            test.filter { f ->
                f.includeTestsMatching("*Test")
                f.includeTestsMatching("*Spec")
                f.isFailOnNoMatchingTests = false
            }
            test.finalizedBy("jacocoTestReport")
        }
        project.tasks.named("jacocoTestReport", JacocoReport::class.java) { report ->
            report.dependsOn("test")
            report.reports {
                it.xml.required.set(true)
            }
        }

        return this
    }
}