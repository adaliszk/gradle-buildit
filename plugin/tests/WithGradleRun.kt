package dev.scaffoldit.test

import io.github.serpro69.kfaker.Faker
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.engine.names.WithDataTestName
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files

val BUILD_FILES = listOf(
    "build.gradle.kts",
    "build.gradle",
)

val SETTING_FILES = listOf(
    "settings.gradle.kts",
    "settings.gradle",
)

val COMMON_BUILDS = BUILD_FILES.map {
    GradleScenario(it, "common")
}

val COMMON_SETTINGS = SETTING_FILES.map {
    GradleScenario(it, "common")
}

val HYTALE_BUILDS = BUILD_FILES.map {
    GradleScenario(it, "hytale")
}

val HYTALE_SETTINGS = SETTING_FILES.map {
    GradleScenario(it, "hytale")
}

data class GradleScenario(
    val scriptFile: String,
    val extensionName: String,
    var randomArg: String? = null,
) : WithDataTestName {
    override fun dataTestName(): String {
        return when (true) {
            (randomArg != null) -> "$extensionName with $randomArg in $scriptFile"
            else -> "$extensionName in $scriptFile"
        }
    }
}

suspend fun <T> withGradle(
    scope: T,
    template: String,
    args: List<String>,
    block: suspend (BuildResult, File, List<String>) -> Unit
) {
    withGradle(scope, template, args, {}, block)
}

suspend fun <T> withGradle(
    scope: T,
    template: String,
    args: List<String>,
    prepare: suspend T.(rootDir: File) -> Unit = {},
    block: suspend (BuildResult, File, List<String>) -> Unit
) {
    @Suppress("BlockingMethodInNonBlockingContext") // Classpath is wrong otherwise
    val tempDir = Files.createTempDirectory("test-").toFile()
    try {
        prepare(scope, tempDir)

        val generatedArgs = mutableListOf<String>()
        if (scope is GradleScenario) {
            scope.randomArg = generatedArgs.joinToString(",")
            val scriptContent = template
                .replace("#extensionName#", scope.extensionName)
                .replace(Regex("#randomName#")) { _ ->
                    val value = Faker().app.name().lowercase().replace(":", "")
                    generatedArgs.add(value)
                    value
                }

            println(scope.scriptFile)
            println("------------------------------------------------------------")
            tempDir.resolve(scope.scriptFile).writeText(scriptContent)
            println(scriptContent)
        }

        tempDir.resolve("gradle.properties").writeText(
            """
                org.gradle.daemon=false
                org.gradle.vfs.watch=false
                org.gradle.configuration-cache=false
                org.gradle.caching=false
            """.trimIndent()
        )

        println("------------------------------------------------------------")
        println("> gradle ${args.joinToString(" ")}")


        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments(args)
            .forwardOutput()
            .build()

        block(result, tempDir, generatedArgs.toList())
    } finally {
        tempDir.deleteRecursively()
    }
}

fun <T> FunSpec.testEachWithGradle(
    scenario: String,
    case: List<T>,
    template: String,
    args: List<String>,
    prepare: suspend T.(rootDir: File) -> Unit = {},
    block: suspend (File, T, BuildResult, List<String>) -> Unit
) {
    context(scenario) {
        withTests(case) { scope ->
            withGradle(scope, template, args, prepare) { result, rootPath, randomArgs ->
                block(rootPath, scope, result, randomArgs)
            }
        }
    }
}