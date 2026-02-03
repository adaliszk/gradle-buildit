package dev.scaffoldit.test.e2e

import dev.scaffoldit.hytale.wire.HytaleManifest
import dev.scaffoldit.test.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome
import kotlin.text.filter


class HytaleManifestSpec : FunSpec({
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
        listOf("projects", "--stacktrace")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        val manifestFile = rootDir.resolve("src/main/resources/manifest.json")
            .also { it.shouldExist() }

        val manifest = HytaleManifest.fromFile(manifestFile)
            .also { it.shouldNotBeNull() }

        val projectName = rootDir.name
            .filter { it.isLetterOrDigit() }
            .replaceFirstChar { it.uppercase() }

        manifest?.Group shouldBe "ScaffoldIt"
        manifest?.Name shouldBe projectName
        manifest?.Version shouldBe "0.0.0"
        manifest?.Main shouldBe "scaffoldit.${projectName.lowercase()}.$projectName"
    }

    testEachWithGradle(
        "with simple manifest configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                manifest {
                    Main = "dev.scaffoldit.example.CustomEntrypoint"
                }
            }
        """.trimIndent(),
        listOf("projects", "--stacktrace")
    ) { rootDir, _, result, _ ->
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No sub-projects"

        val manifestFile = rootDir.resolve("src/main/resources/manifest.json")
            .also { it.shouldExist() }

        val manifest = HytaleManifest.fromFile(manifestFile)
            .also { it.shouldNotBeNull() }

        val projectName = rootDir.name
            .filter { it.isLetterOrDigit() }
            .replaceFirstChar { it.uppercase() }

        manifest?.Group shouldBe "ScaffoldIt"
        manifest?.Name shouldBe projectName
        manifest?.Version shouldBe "0.0.0"
        manifest?.Main shouldBe "dev.scaffoldit.example.CustomEntrypoint"
    }
})