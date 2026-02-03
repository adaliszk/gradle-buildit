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
import java.io.File


class CommonOnlyScriptSpec : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    testEachWithGradle(
        "without any configuration and folder",
        COMMON_BUILDS + COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
                // Nothing configured
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        rootDir.resolve("common").shouldNotExist()

        rootDir.resolve("common/src/main/resources").shouldNotExist()
        rootDir.resolve("common/src/main/java").shouldNotExist()

        rootDir.resolve("common/src/test/resources").shouldNotExist()
        rootDir.resolve("common/src/test/java").shouldNotExist()
    }

    testEachWithGradle(
        "without any configuration but with folder",
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
                // Nothing configured
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        rootDir.resolve("common").shouldExist()

        rootDir.resolve("common/src/main/resources").shouldExist()
        rootDir.resolve("common/src/main/java").shouldExist()

        rootDir.resolve("common/src/test/resources").shouldExist()
        rootDir.resolve("common/src/test/java").shouldExist()
    }

    testEachWithGradle(
        "useKotlin() with common folder",
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
                useKotlin()
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        rootDir.resolve("common").shouldExist()

        rootDir.resolve("common/src/main/resources").shouldExist()
        rootDir.resolve("common/src/test/kotlin").shouldExist()
        rootDir.resolve("common/src/test/resources").shouldExist()
        rootDir.resolve("common/src/test/kotlin").shouldExist()

        rootDir.resolve("common/src/main/java").shouldNotExist()
        rootDir.resolve("common/src/test/java").shouldNotExist()
    }

    testEachWithGradle(
        "useFlat() with common folder",
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
                useFlat()
            } 
        """.trimIndent(),
        listOf("projects", "--stacktrace"),
        {
            it.resolve("common").mkdirs()
        }
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldNotContain "No sub-projects"

        rootDir.resolve("common").shouldExist()

        rootDir.resolve("common/resources").shouldExist()
        rootDir.resolve("common/source").shouldExist()

        rootDir.resolve("common/src/main/resources").shouldNotExist()
        rootDir.resolve("common/src/main/java").shouldNotExist()
        rootDir.resolve("common/src/test/resources").shouldNotExist()
        rootDir.resolve("common/src/test/java").shouldNotExist()
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
        rootDir.resolve("common/$name/src/main/resources").shouldExist()
        rootDir.resolve("common/$name/src/main/java").shouldExist()
        rootDir.resolve("common/$name/src/test/resources").shouldExist()
        rootDir.resolve("common/$name/src/test/java").shouldExist()
        rootDir.resolve("common/common").shouldNotExist()
        rootDir.resolve("common/$name/common").shouldNotExist()
        rootDir.resolve("common/$name/src/common").shouldNotExist()
    }

    testEachWithGradle(
        "with single include() using single value",
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
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
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
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
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
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
        COMMON_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            common {
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