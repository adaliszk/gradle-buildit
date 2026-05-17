// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

/**
 * Resolves the version published as `<latest>` in a Maven `maven-metadata.xml`.
 *
 * Gradle's `+` / `latest.integration` / `latest.release` selectors all sort the
 * `<versions>` list by Gradle's StaticVersionComparator and ignore `<latest>`,
 * so a publisher who switches version schemes (e.g. `2026.05.07-abc` → `0.5.0-pre.8`)
 * sees Gradle pick an older build. Fetching `<latest>` directly honors the
 * publisher's intent. See gradle/gradle#9141.
 *
 * Implemented as a `ValueSource` so the HTTP fetch participates in the Gradle
 * configuration cache rather than running on every configuration phase.
 */
abstract class MavenMetadataLatestVersion :
    ValueSource<String, MavenMetadataLatestVersion.Parameters> {

    interface Parameters : ValueSourceParameters {
        val metadataUrl: Property<String>
        val fallback: Property<String>
    }

    override fun obtain(): String {
        val log = Logging.getLogger(MavenMetadataLatestVersion::class.java)
        val url = parameters.metadataUrl.get()
        val fallback = parameters.fallback.get()
        return try {
            when (val latest = readLatestElement(url)) {
                null -> {
                    log.warn("> Hytale: $url missing <latest> tag, falling back to '$fallback'")
                    fallback
                }
                else -> {
                    log.lifecycle("> Hytale: useVersion(\"latest\") resolved to '$latest' via maven-metadata.xml")
                    latest
                }
            }
        } catch (e: Exception) {
            log.warn("> Hytale: could not read $url, falling back to '$fallback': ${e.message}")
            fallback
        }
    }

    private fun readLatestElement(url: String): String? {
        val factory = XMLInputFactory.newInstance().apply {
            setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
            setProperty(XMLInputFactory.SUPPORT_DTD, false)
        }
        java.net.URI(url).toURL().openStream().use { stream ->
            val reader = factory.createXMLStreamReader(stream)
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT &&
                        reader.localName == "latest"
                    ) {
                        return reader.elementText
                    }
                }
            } finally {
                reader.close()
            }
        }
        return null
    }
}
