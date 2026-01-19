@file:Suppress("PropertyName") // This is a DTO where the "manifest.json" dictates the name
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package dev.buildit.gradle.hytale

import dev.buildit.gradle.BuildItExtension
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.io.File


@Serializable
data class HytaleManifest(
    val Group: String?,
    val Name: String?,
    var Version: String?,
    val Description: String?,
    val Authors: List<Author>,
    val Website: String?,
    val DisabledByDefault: Boolean = false,
    var IncludesAssetPack: Boolean = false,
    val Dependencies: Map<String, String> = emptyMap(),
    val OptionalDependencies: Map<String, String> = emptyMap(),
    val ServerVersion: String?,
    val Main: String?,
) {
    @Transient
    private var filePath: File? = null

    @Serializable
    data class Author(
        val Name: String,
        val Role: String? = null,
    )

    fun update(block: HytaleManifest.() -> Unit = {}): HytaleManifest {
        this.apply(block)

        val file = filePath ?: throw IllegalStateException(
            "File path not set. Manifest must be created with HytalePluginManifest.from(project) before calling update().",
        )

        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(this))

        return this
    }

    companion object {
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        fun from(project: Project): HytaleManifest {
            val file = findManifestFile(project)
            val settings = BuildItExtension(lazy { project })

            return buildManifest(file) {
                HytaleManifest(
                    Group = settings.group,
                    Name = settings.name,
                    Version = settings.version,
                    Description = settings.description,
                    Authors = listOf(),
                    Website = settings.website,
                    ServerVersion = settings.gameVersion.let { version ->
                        when {
                            version.first().isLetterOrDigit() -> "=$version"
                            version.isBlank() -> "*"
                            else -> version
                        }
                    },
                    Main = settings.mainClass.takeIf { settings.mainFile != null },
                    IncludesAssetPack = true
                )
            }
        }

        private fun findManifestFile(project: Project): File {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets.getByName("main")

            return mainSourceSet.resources.srcDirs
                .firstNotNullOfOrNull { File(it, "manifest.json").takeIf { f -> f.exists() } }
                ?: File(mainSourceSet.resources.srcDirs.first(), "manifest.json")
        }

        private inline fun <reified T> buildManifest(file: File, block: () -> T): T {
            val existing = file.takeIf { it.exists() }
                ?.let { json.parseToJsonElement(it.readText()).jsonObject.toMutableMap() }
                ?: mutableMapOf()

            val newManifest = json.encodeToJsonElement(block()).jsonObject
            existing.putAll(newManifest)

            return json.decodeFromJsonElement<T>(JsonObject(existing)).also {
                if (it is HytaleManifest) {
                    it.filePath = file
                }
            }
        }
    }
}