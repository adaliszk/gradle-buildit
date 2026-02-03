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


    lateinit var project: Project
    var useKotlin: Boolean = false

    fun configure(project: Project, parent: Gradle.ConfigureToolchain): TestingEngine {
        val junitSupport = project.providers.gradleProperty("env.scaffoldit.javaUnitTest")
            .getOrElse("true").toBoolean()
        val kotestSupport = project.providers.gradleProperty("env.scaffoldit.kotlinTest")
            .getOrElse("true").toBoolean()
        if (!junitSupport && !kotestSupport) return this

        this.useKotlin = parent.kotlin != null
        this.project = project

        project.dependencies.apply {
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        }

        project.tasks.named("test", Test::class.java) { test ->
            test.useJUnitPlatform()
            test.filter { f ->
                f.includeTestsMatching("*Test")
                f.includeTestsMatching("*Spec")
                f.isFailOnNoMatchingTests = false
            }
        }

        configureJacocoCoverage()

        @Suppress("KotlinConstantConditions") // They are not constant
        when (true) {
            kotestSupport -> configureKotlinTest()
            junitSupport -> configureJavaUnitTest()
            else -> return this
        }

        return this
    }

    private fun configureJavaUnitTest() {
        val version = project.providers.gradleProperty("env.scaffoldit.javaUnitTest.version")
            .getOrElse("5.10.1") // TODO: Expose this to the build process via .properties

        project.dependencies.apply {
            add("testImplementation", "org.junit.jupiter:junit-jupiter:$version")
        }
    }

    private fun configureKotlinTest() {
        val version = project.providers.gradleProperty("env.scaffoldit.kotlinTest.version")
            .getOrElse("6.1.1") // TODO: Expose this to the build process via .properties

        project.dependencies.apply {
            add("testImplementation", "io.kotest:kotest-runner-junit5:$version")
            add("testImplementation", "io.kotest:kotest-assertions-core:$version")
            add("testImplementation", "io.kotest:kotest-property:$version")
        }
    }

    private fun configureJacocoCoverage() {
        val supportEnabled = project.providers.gradleProperty("env.scaffoldit.jacocoCoverage")
            .getOrElse("true").toBoolean()
        if (!supportEnabled) return

        project.pluginManager.apply("org.gradle.jacoco")

        project.tasks.named("jacocoTestReport", JacocoReport::class.java) { report ->
            report.dependsOn("test")
            report.reports {
                it.xml.required.set(true)
            }
        }

        project.tasks.named("test", Test::class.java) { test ->
            test.finalizedBy("jacocoTestReport")
        }
    }
}