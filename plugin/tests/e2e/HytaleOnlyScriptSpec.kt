package dev.scaffoldit.test.e2e

import dev.scaffoldit.gradle.tasks.NestedProjects
import dev.scaffoldit.test.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome


class HytaleOnlyScriptSpec : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    testEachWithGradle(
        "without any configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            #extensionName# {
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
            #extensionName# {
                useKotlin()
            } 
        """.trimIndent(),
        listOf("projects"),
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
            #extensionName# {
                useFlat()
            } 
        """.trimIndent(),
        listOf("projects")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        rootDir.resolve("assets").shouldExist()
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

    testEachWithGradle(
        "no configuration with single include()",
        HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            #extensionName# {
                include("#randomName#")
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
    ) { rootDir, case, result, randomArgs ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        rootDir.resolve("assets").shouldExist()
        rootDir.resolve("src/main/resources").shouldNotExist()
        rootDir.resolve("src/main/kotlin").shouldNotExist()
        rootDir.resolve("src/test/resources").shouldNotExist()
        rootDir.resolve("src/test/kotlin").shouldNotExist()
        rootDir.resolve("src/main/java").shouldNotExist()
        rootDir.resolve("src/test/java").shouldNotExist()

        randomArgs.forEach { randomName ->
            rootDir.resolve("${case.extensionName}/$randomName/src/main/resources").shouldExist()
            rootDir.resolve("${case.extensionName}/$randomName/src/main/java").shouldExist()
            rootDir.resolve("${case.extensionName}/$randomName/src/test/resources").shouldExist()
            rootDir.resolve("${case.extensionName}/$randomName/src/test/java").shouldExist()
        }
    }
})