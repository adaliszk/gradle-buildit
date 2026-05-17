// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.api.VERSION
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.hytale.wire.HytaleGradle
import dev.scaffoldit.hytale.wire.HytaleManifest
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy

class HytaleServerPlatform() : HytaleGradle.ConfigurePlatform {
    private val log: Logger = Logging.getLogger(this::class.java)

    override var patchline: Patchline = Patchline.RELEASE

    override var version: String = "+"

    private var pendingManifest: (HytaleManifest.() -> Unit)? = null

    override fun manifest(config: HytaleManifest.() -> Unit) {
        pendingManifest = config
    }

    override fun manifest(action: Action<HytaleManifest>) {
        pendingManifest = { action.execute(this) }
    }

    lateinit var project: Project

    fun configure(
        project: Project,
        parent: HytaleGradle.ConfigurePlatform
    ): Gradle.ConfigurePlatform {
        this.project = project
        this.patchline = parent.patchline

        registerManifestGenerationTask()
        configureHytaleMaven()
        configureDevserverAgent()

        project.afterEvaluate {
            val manifestGenerator = project.providers
                .gradleProperty("env.hytale.manifestGenerator")
                .getOrElse("true").toBoolean()
            if (!manifestGenerator) return@afterEvaluate

            pendingManifest?.let { config ->
                HytaleManifest.from(project).apply(config).configure(project)
            }
            HytaleManifest.from(project).saveTo(project)
        }

        return this
    }

    fun registerManifestGenerationTask() {
        val manifestGenerator = project.providers
            .gradleProperty("env.hytale.manifestGenerator")
            .getOrElse("true").toBoolean()
        if (!manifestGenerator) return

        val manifest = HytaleManifest.from(project)
        with(project.tasks) {
            val generateManifest = maybeCreate("generateManifest").apply {
                description = "Generate the plugin manifest from settings and existing values."
                group = "hytale"
                doFirst {
                    manifest.save()
                }
            }
            named("processResources", Copy::class.java) { task ->
                task.dependsOn(generateManifest)
                task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }

    fun configureHytaleMaven() {
        with(project.repositories) {
            log.lifecycle("> Repos :${project.name}.maven('https://maven.hytale.com/${patchline.repo}')")
            maven {
                it.url = project.uri("https://maven.hytale.com/${patchline.repo}")
            }
        }
        val versionDisplay = if (version == "latest") "<latest, resolved lazily from maven-metadata.xml>" else version
        log.lifecycle("> Deps :${project.name}.implementation('com.hypixel.hytale:Server:$versionDisplay')")
        project.dependencies.addProvider(
            "implementation",
            serverVersionProvider().map { "com.hypixel.hytale:Server:$it" }
        )
    }

    private fun serverVersionProvider(): Provider<String> {
        if (version != "latest") {
            return project.provider { version }
        }
        val patchlineRepo = patchline.repo
        return project.providers.of(MavenMetadataLatestVersion::class.java) { spec ->
            spec.parameters.metadataUrl.set(
                "https://maven.hytale.com/$patchlineRepo/com/hypixel/hytale/Server/maven-metadata.xml"
            )
            spec.parameters.fallback.set("+")
        }
    }

    fun configureDevserverAgent() {
        val devServerHotReload = project.providers
            .gradleProperty("env.hytale.devServerHotReload")
            .getOrElse("true").toBoolean()
        if (!devServerHotReload) return

        with(project.dependencies) {
            log.lifecycle("> Deps :${project.name}.runtimeOnly('dev.scaffoldit:devtools:${VERSION}')")
            add(
                "runtimeOnly",
                "dev.scaffoldit:devtools:${VERSION}"
            )
        }
    }
}