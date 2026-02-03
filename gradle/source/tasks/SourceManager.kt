// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.Language
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

import org.gradle.api.logging.Logger
import java.io.File
import javax.xml.transform.Source
import org.gradle.api.logging.Logging as GradleLogger

class SourceManager : Gradle.ConfigurePaths {
    private val log: Logger = GradleLogger.getLogger(this::class.java)

    override var projectDir: String = ""

    var packageDir: String = ""

    var flatLayout: Boolean = false

    override fun useFlat() {
        log.lifecycle("SourceManager.useFlat(current=$flatLayout)")
        flatLayout = true
    }

    var language: Language = Language.JAVA
    val sourceDir: String get() = "src/main/${language.dir}"
    var sourceResourceDir: String = "src/main/resources"
    val testsDir: String get() = "src/test/${language.dir}"
    var testResourceDir: String = "src/test/resources"
    var assetsDir: String = "assets"

    // region Internal APIs

    lateinit var project: Project

    fun configure(project: Project, parent: Wired): SourceManager {
        this.project = project

        val toolchain = parent.with(ToolchainManager::class)
        this.language = if (toolchain.kotlin != null) Language.KOTLIN else Language.JAVA

        val source = parent.with(SourceManager::class)
        this.flatLayout = this.flatLayout || source.flatLayout
        this.projectDir = source.projectDir

        val layout = resolveLayout(flatLayout)
        val assetsPath = bootstrapAssets(layout)
        bootstrapSources(layout)

        log.lifecycle("> Source :${project.name}:$language(flat=$flatLayout) in ${project.file(layout.srcPath).canonicalPath}")

        fun <T : Any> NamedDomainObjectContainer<T>.configureSourceDirs(
            resources: T.() -> SourceDirectorySet,
            source: T.() -> SourceDirectorySet
        ) {
            named("main") { main ->
                main.resources().setSrcDirs(listOf(project.file(layout.srcResourcePath)))
                main.source().setSrcDirs(listOf(project.file(layout.srcPath)))
            }
            if (assetsPath !== null) {
                findByName("assets") ?: create("assets") { assets ->
                    assets.resources().setSrcDirs(listOf(assetsPath))
                }
            }
            named("test") { test ->
                test.resources().setSrcDirs(listOf(project.file(layout.testResourcePath)))
                test.source().setSrcDirs(listOf(project.file(layout.testPath)))
            }
        }

        when (language) {
            Language.KOTLIN -> {
                project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
                    .configureSourceDirs({ resources }, { kotlin })
            }

            Language.JAVA -> {
                project.extensions.getByType(SourceSetContainer::class.java)
                    .configureSourceDirs({ resources }, { java })
            }
        }

        return this
    }

    private fun bootstrapSources(layout: SourceLayout) {
        project.file(layout.srcResourcePath).mkdirs()
        project.file(layout.srcPath).mkdirs()
        project.file(layout.testResourcePath).mkdirs()
        project.file(layout.testPath).mkdirs()
    }

    private fun bootstrapAssets(layout: SourceLayout): File? {
        val assetsEnabled = project.gradle.rootProject.providers
            .gradleProperty("env.scaffoldit.globalAssets")
            .getOrElse("true").toBoolean()
        if (!assetsEnabled) return null

        val assetsPath = project.gradle.rootProject.file(layout.assetsPath).also {
            it.mkdirs()
        }
        log.lifecycle("> Assets :${project.name} in ${assetsPath.canonicalPath}")
        return assetsPath
    }

    // endregion

    private data class SourceLayout(
        val srcPath: String,
        val srcResourcePath: String,
        val assetsPath: String,
        val testPath: String,
        val testResourcePath: String,
    )

    private fun resolveLayout(flatLayout: Boolean): SourceLayout {
        return when (flatLayout) {
            true -> SourceLayout(
                srcPath = "source",
                srcResourcePath = "resources",
                assetsPath = assetsDir,
                testPath = "source",
                testResourcePath = "resources",
            )

            else -> SourceLayout(
                srcPath = "$sourceDir/$packageDir",
                srcResourcePath = sourceResourceDir,
                assetsPath = assetsDir,
                testPath = testsDir,
                testResourcePath = testResourceDir,
            )
        }
    }
}