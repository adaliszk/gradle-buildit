package dev.scaffoldit.test

import io.kotest.core.spec.style.FunSpec
import java.io.File
import java.nio.file.Files

abstract class TestScenario(body: FunSpec.() -> Unit) : FunSpec({
    lateinit var projectDir: File

    beforeTest {
        projectDir = Files.createTempDirectory("test-").toFile()
    }

    afterTest {
        projectDir.deleteRecursively()
    }

    body()
})