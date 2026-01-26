package dev.scaffoldit.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files


class SanityTest : TestScenario({
    lateinit var projectDir: File

    beforeTest {
        projectDir = Files.createTempDirectory("gradle-test").toFile()
    }

    afterTest {
        projectDir.deleteRecursively()
    }

    test("plugin applies no changes without config") {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            plugins {
                id("dev.scaffoldit")
            }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("projects", "--stacktrace")
            .build()

        result.output shouldNotContain "> Task :common"
        result.output shouldNotContain "> Task :hytale"
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
    }
})