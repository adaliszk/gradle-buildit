// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.test.e2e

import dev.scaffoldit.test.*
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome


class HytaleDevserverSpec : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

// TODO: Resolve the Assets file before re-enabling these!
//
//    testEachWithGradle(
//        "without any configuration",
//        HYTALE_BUILDS + HYTALE_SETTINGS,
//        """
//            plugins {
//                id("dev.scaffoldit")
//            }
//            hytale {
//                // Nothing configured
//            }
//        """.trimIndent(),
//        listOf("tasks", "--stacktrace")
//    ) { rootDir, _, result, _ ->
//        result.task(":tasks")?.outcome shouldBe TaskOutcome.SUCCESS
//        result.output shouldContain "setupServer"
//        result.output shouldContain "runServer"
//
//        rootDir.resolve("devserver").shouldExist()
//    }

    testEachWithGradle(
        "with disabling the devserver",
        HYTALE_BUILDS + HYTALE_SETTINGS,
        """
            plugins {
                id("dev.scaffoldit")
            }
            hytale {
                devserver {
                    Enabled = false
                }
            }
        """.trimIndent(),
        listOf("tasks", "--stacktrace")
    ) { rootDir, _, result, _ ->
        result.task(":tasks")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "setupServer"
        result.output shouldContain "runServer"

        rootDir.resolve("devserver").shouldNotExist()
    }
})