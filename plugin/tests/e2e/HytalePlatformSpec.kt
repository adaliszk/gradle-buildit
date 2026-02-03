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


class HytalePlatformSpec : FunSpec({
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
        listOf()
    ) { _, _, result, _ ->
        result.output shouldContain "'https://maven.hytale.com/release'"
    }

    testEachWithGradle(
        "with enum-based configuration",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            import dev.scaffoldit.hytale.Patchline
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                patchline = Patchline.PRE_RELEASE
            }
        """.trimIndent(),
        listOf()
    ) { _, _, result, _ ->
        result.output shouldContain "'https://maven.hytale.com/pre-release'"
    }
})