package dev.scaffoldit.test.e2e

import dev.scaffoldit.test.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome
import java.io.File


class HytaleOnlyScriptSpec : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    testEachWithGradle(
        "without any configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                // Nothing configured
            } 
        """.trimIndent(),
        listOf("projects")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        rootDir.resolve("assets").shouldExist()

        rootDir.resolve("src/main/resources").shouldExist()
        rootDir.resolve("src/main/java").shouldExist()

        rootDir.resolve("src/test/resources").shouldExist()
        rootDir.resolve("src/test/java").shouldExist()
    }

    testEachWithGradle(
        "useKotlin() configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                useKotlin()
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        rootDir.resolve("assets").shouldExist()
        rootDir.resolve("src/main/resources").shouldExist()
        rootDir.resolve("src/main/kotlin").shouldExist()
        rootDir.resolve("src/test/resources").shouldExist()
        rootDir.resolve("src/test/kotlin").shouldExist()

        rootDir.resolve("src/main/java").shouldNotExist()
        rootDir.resolve("src/test/java").shouldNotExist()
    }

    testEachWithGradle(
        "useFlat() configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                useFlat()
            } 
        """.trimIndent(),
        listOf("projects")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        rootDir.resolve("assets").shouldExist()
        rootDir.resolve("resources").shouldExist()
        rootDir.resolve("source").shouldExist()

        rootDir.resolve("src/main/java").shouldNotExist()
        rootDir.resolve("src/test/java").shouldNotExist()
        rootDir.resolve("src/main/resources").shouldNotExist()
        rootDir.resolve("src/main/kotlin").shouldNotExist()
        rootDir.resolve("src/test/resources").shouldNotExist()
        rootDir.resolve("src/test/kotlin").shouldNotExist()
    }

    fun assertRootSkipped(rootDir: File) {
        rootDir.resolve("assets").shouldExist()
        rootDir.resolve("resources").shouldNotExist()
        rootDir.resolve("source").shouldNotExist()
        rootDir.resolve("src/main/resources").shouldNotExist()
        rootDir.resolve("src/main/kotlin").shouldNotExist()
        rootDir.resolve("src/test/resources").shouldNotExist()
        rootDir.resolve("src/test/kotlin").shouldNotExist()
        rootDir.resolve("src/main/java").shouldNotExist()
        rootDir.resolve("src/test/java").shouldNotExist()
    }

    fun assertWorkspace(rootDir: File, name: String) {
        rootDir.resolve("hytale/$name/src/main/resources").shouldExist()
        rootDir.resolve("hytale/$name/src/main/java").shouldExist()
        rootDir.resolve("hytale/$name/src/test/resources").shouldExist()
        rootDir.resolve("hytale/$name/src/test/java").shouldExist()
        rootDir.resolve("hytale/hytale").shouldNotExist()
        rootDir.resolve("hytale/$name/hytale").shouldNotExist()
        rootDir.resolve("hytale/$name/src/hytale").shouldNotExist()
    }

    testEachWithGradle(
        "with single include() using single value",
        HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                include("#randomName#")
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, randomArgs ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        randomArgs.forEach { randomName ->
            assertWorkspace(rootDir, randomName)
        }

        assertRootSkipped(rootDir)
    }

    testEachWithGradle(
        "with single include() using multiple values",
        HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                include("#randomName#", "#randomName#", "#randomName#")
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, randomArgs ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        randomArgs.forEach { randomName ->
            assertWorkspace(rootDir, randomName)
        }

        assertRootSkipped(rootDir)
    }

    testEachWithGradle(
        "with multiple include() using single values",
        HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                include("#randomName#")
                include("#randomName#")
                include("#randomName#")
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, randomArgs ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        randomArgs.forEach { randomName ->
            assertWorkspace(rootDir, randomName)
        }

        assertRootSkipped(rootDir)
    }

    testEachWithGradle(
        "with multiple include() using multiple values",
        HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                include("#randomName#", "#randomName#", "#randomName#")
                include("#randomName#", "#randomName#", "#randomName#")
                include("#randomName#", "#randomName#", "#randomName#")
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, randomArgs ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        randomArgs.forEach { randomName ->
            assertWorkspace(rootDir, randomName)
        }

        assertRootSkipped(rootDir)
    }
})