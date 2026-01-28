package dev.scaffoldit.test.e2e

import dev.scaffoldit.test.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome


class SanityCheckTestingSpec : FunSpec({
    testEachWithGradle(
        "plugin applies no changes without config",
        COMMON_BUILDS + COMMON_SETTINGS + HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            // Empty file without plugins (sanity check)
        """.trimIndent(),
        listOf("projects")
    ) { rootDir, _, result, _ ->
        result.output shouldNotContain "> Task :common"
        result.output shouldNotContain "> Task :hytale"
        result.task(":projects")?.outcome shouldBe TaskOutcome.SUCCESS

        rootDir.resolve("assets").shouldNotExist()
        rootDir.resolve("src/main/resources").shouldNotExist()
        rootDir.resolve("src/main/java").shouldNotExist()
        rootDir.resolve("src/test/resources").shouldNotExist()
        rootDir.resolve("src/test/java").shouldNotExist()
    }
})