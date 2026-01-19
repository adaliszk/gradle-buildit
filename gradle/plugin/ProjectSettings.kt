package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

class ProjectSettings(private val project: Project)
{
    val meta = ProjectMetadata.from(project)

    val group = project.config("project.group").toTitlecase()
    val name = project.name.toTitlecase()
    val version = project.config("project.version")
    val description = project.config("project.description")
    val website = project.config("project.website")

    val mainClass = listOfNotNull(
        project.config("env.maven.group"),
        meta.type.packageGroup ?: meta.packageName.lowercase(),
        when (meta.type) {
            ProjectMetadata.Type.HYTALE -> "${meta.packageName.toTitlecase()}Plugin"
            else -> group + meta.packageName.toTitlecase()
        }
    ).joinToString(".")

    fun mainFile(className: String, project: Project): File? {
        val classPath = className.replace(".", File.separator)
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName("main")

        return mainSourceSet.allSource.firstOrNull { file ->
            file.absolutePath.endsWith("$classPath.kt") ||
                file.absolutePath.endsWith("$classPath.java")
        }
    }

    private fun String.toTitlecase() = replaceFirstChar { it.uppercase() }

    private fun Project.config(name: String, default: String? = null): String =
        providers.gradleProperty(name).let {
            if (default != null) it.orElse(default) else it
        }.get()
}