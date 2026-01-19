package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

@Suppress("unused") // Used by Gradle, but that is not visible
open class BuildItExtension(pending: Lazy<Project>) : GradleExtension(pending) {
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
        kotlinLibrary = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        sourceDir = "src/main/kotlin"
        resourcesDir = "src/main/resources"
        testsDir = "src/test/kotlin"
    }

    fun useFlat() {
        TODO("Implement source abstraction handler")
    }

    val mainClass by lazy {
        listOfNotNull(
            config("env.maven.group"),
            config("project.name"),
            config("env.maven.name").toTitlecase() + "Plugin",
        ).joinToString(".")
    }

    val mainFile: File? by lazy {
        val classPath = mainClass.replace(".", File.separator)
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName("main")

        mainSourceSet.allSource.firstOrNull { file ->
            file.absolutePath.endsWith("$classPath.kt") ||
                file.absolutePath.endsWith("$classPath.java")
        }
    }


    private fun config(property: String, default: String = ""): String {
        return project.providers.gradleProperty(property).getOrElse(default)
    }

    private fun String.toTitlecase() = replaceFirstChar { it.uppercase() }
}