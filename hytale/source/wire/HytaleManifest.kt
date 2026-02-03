// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale.wire

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.extensions.core.extra
import java.io.File

@Suppress("PropertyName") // This is how the JSON is structured, so we reflect that!
@Serializable
data class HytaleManifest(
    var Group: String? = null,
    var Name: String? = null,
    var Version: String? = null,
    var Description: String? = null,
    var Authors: List<Author> = emptyList(),
    var Website: String? = null,
    var DisabledByDefault: Boolean = false,
    var IncludesAssetPack: Boolean = false,
    var Dependencies: Map<String, String>? = null,
    var OptionalDependencies: Map<String, String>? = null,
    var LoadBefore: Map<String, String>? = null,
    var ServerVersion: String = "*",
    var Main: String? = null,
    var SubPlugins: List<HytaleManifest>? = null,
) {
    @Serializable
    data class Author(
        val Name: String,
        val Email: String? = null,
        val Url: String? = null,
    )

    fun saveTo(project: Project): Boolean {
        return runCatching {
            val file = findManifestFile(project)
            file.parentFile.mkdirs()
            file.writeText(json.encodeToString(serializer(), this))
            true
        }.getOrElse { false }
    }

    fun configure(project: Project) {
        project.extra.set("hytaleGroup", Group)
        project.extra.set("hytaleName", Name)
        project.extra.set("hytaleVersion", Version)
        project.extra.set("hytaleDescription", Description)
        project.extra.set("hytaleAuthors", Authors)
        project.extra.set("hytaleWebsite", Website)
        project.extra.set("hytaleDisabledByDefault", DisabledByDefault)
        project.extra.set("hytaleIncludesAssetPack", IncludesAssetPack)
        project.extra.set("hytaleDependencies", Dependencies)
        project.extra.set("hytaleOptionalDependencies", OptionalDependencies)
        project.extra.set("hytaleLoadBefore", LoadBefore)
        project.extra.set("hytaleServerVersion", ServerVersion)
        project.extra.set("hytaleMain", Main)
        project.extra.set("hytaleSubPlugins", SubPlugins)
    }

    companion object {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        fun findManifestFile(project: Project): File {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets.getByName("main")

            return mainSourceSet.resources.srcDirs
                .firstNotNullOfOrNull { File(it, "manifest.json").takeIf { f -> f.exists() } }
                ?: File(mainSourceSet.resources.srcDirs.first(), "manifest.json")
        }

        fun fromFile(file: File): HytaleManifest? {
            return if (file.exists()) {
                json.decodeFromString(serializer(), file.readText())
            } else {
                null
            }
        }

        fun from(project: Project): HytaleManifest {
            val projectGroup = project.group.toString()
                .filter { it.isLetterOrDigit() }
                .substringAfterLast(".").replaceFirstChar { it.uppercase() }
                .ifBlank { "ScaffoldIt" }
            val projectName = project.name
                .filter { it.isLetterOrDigit() }
                .replaceFirstChar { it.uppercase() }
                .ifBlank { "NameMe" }
            val projectVersion = project.version.toString()
                .replace("unspecified", "0.0.0")

            val current = findManifestFile(project).let { file ->
                if (file.exists()) {
                    json.decodeFromString(serializer(), file.readText())
                } else {
                    HytaleManifest(
                        Group = projectGroup,
                        Name = projectName,
                        Version = projectVersion,
                    )
                }
            }

            val mainPackage: String = "${projectGroup}.${projectName}"
                .replace("-", ".")
                .replace("_", ".")
                .lowercase()

            val mainClassName: String = projectName

            with(project.extra) {
                @Suppress("UNCHECKED_CAST") // The type here _should_ be correct
                return current.copy(
                    Group = properties["hytaleGroup"] as? String
                        ?: current.Group
                        ?: projectGroup,
                    Name = properties["hytaleName"] as? String
                        ?: current.Name
                        ?: projectName,
                    Version = properties["hytaleVersion"] as? String
                        ?: current.Version
                        ?: projectVersion,
                    Description = properties["hytaleDescription"] as? String
                        ?: current.Description,
                    Authors = properties.deserialize("hytaleAuthors", current.Authors),
                    Website = properties["hytaleWebsite"] as? String
                        ?: current.Website,
                    DisabledByDefault = properties["hytaleDisabledByDefault"] as? Boolean
                        ?: current.DisabledByDefault,
                    IncludesAssetPack = properties["hytaleIncludesAssetPack"] as? Boolean
                        ?: current.IncludesAssetPack,
                    Dependencies = properties.deserialize(
                        "hytaleDependencies",
                        current.Dependencies
                    ),
                    OptionalDependencies = properties.deserialize(
                        "hytaleOptionalDependencies",
                        current.OptionalDependencies
                    ),
                    LoadBefore = properties.deserialize(
                        "hytaleLoadBefore",
                        current.LoadBefore
                    ),
                    ServerVersion = properties.deserialize(
                        "hytaleServerVersion",
                        current.ServerVersion
                    ),
                    Main = properties["hytaleMain"] as? String
                        ?: current.Main
                        ?: "${mainPackage}.${mainClassName}",
                    SubPlugins = properties["hytaleSub"] as? List<HytaleManifest>
                        ?: current.SubPlugins
                )
            }
        }

        private inline fun <reified T> Map<String, Any?>.deserialize(key: String, fallback: T): T {
            return this[key] as T? ?: fallback
        }

    }
}