package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@Suppress("unused") // Used by Gradle, but that is not visible
open class BuildItExtension(pending: Lazy<Project>) : GradleExtension(pending) {
    private val log: Logger = Logging.getLogger(BuildItExtension::class.java)

    val group by lazy { config("project.group").toTitlecase() }
    val name by lazy { project.name.toTitlecase() }
    val version by lazy { config("project.version") }
    val gameVersion by lazy { config("hytale.version") }
    val description by lazy { config("project.description") }
    val website by lazy { config("project.website") }

    companion object {
        var sourceDir: String = "src/main/java"
        var resourcesDir: String = "src/main/resources"
        var testsDir: String = "src/test/java"
        var assetDir: String = "assets"

        /** @internal */
        var kotlinLibrary: String = ""
    }

    fun useKotlin(dependencyNotation: String? = null) {
        log.lifecycle("> Plug :useKotlin()")

        kotlinLibrary = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        sourceDir = "src/main/kotlin"
        resourcesDir = "src/main/resources"
        testsDir = "src/test/kotlin"
    }

    fun useFlat() {
        TODO("Implement source abstraction handler")
    }
}