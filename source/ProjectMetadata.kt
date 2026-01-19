package dev.buildit.gradle

import org.gradle.api.Project
import kotlin.text.lowercase


data class ProjectMetadata(
    var packageName: String,
    var type: Type = Type.UNKNOWN,
    var options: Map<String, Any> = mapOf()
)
{
    enum class Type(val packageGroup: String?)
    {
        HYTALE("hytale"),
        LIBRARY(null),
        UNKNOWN(null),
    }

    companion object
    {
        fun from(project: Project): ProjectMetadata
        {
            with(project.extensions.extraProperties) {
                return ProjectMetadata(
                    type = properties["type"] as? Type ?: Type.UNKNOWN,
                    packageName = properties["packageName"] as? String ?: project.name,
                )
            }
        }
    }

    fun update(project: Project)
    {
        project.extensions.extraProperties.set("type", type)
        project.extensions.extraProperties.set("packageName", packageName.lowercase())
    }
}