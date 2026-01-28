// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.Language
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

import org.gradle.api.logging.Logger
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

    private var useKotlin: Boolean = false

    fun withKotlin(kotlin: String?): SourceManager {
        if (kotlin != null) {useKotlin = true}
        return this
    }

    lateinit var project: Project

    fun configure() {
        if (::project.isInitialized) configure(project)
    }

    fun configure(project: Project): SourceManager {
        this.language = if (useKotlin) Language.KOTLIN else Language.JAVA
        this.project = project

        val layout = resolveLayout()
        project.file(layout.srcResourcePath).mkdirs()
        project.file(layout.srcPath).mkdirs()
        val assetsPath = project.gradle.rootProject.file(layout.assetsPath).also {
            it.mkdirs()
        }
        log.lifecycle("> Assets in ${assetsPath.canonicalPath}")
        project.file(layout.testResourcePath).mkdirs()
        project.file(layout.testPath).mkdirs()

        log.lifecycle("> Source :$language(flat=$flatLayout) in ${project.file(layout.srcPath).canonicalPath}")

        fun <T : Any> NamedDomainObjectContainer<T>.configureSourceDirs(
            resources: T.() -> SourceDirectorySet,
            source: T.() -> SourceDirectorySet
        ) {
            named("main") { main ->
                main.resources().setSrcDirs(listOf(project.file(layout.srcResourcePath)))
                main.source().setSrcDirs(listOf(project.file(layout.srcPath)))
            }
            // TODO: Move [assets] to be opt-in feature
            findByName("assets") ?: create("assets") { assets ->
                assets.resources().setSrcDirs(listOf(assetsPath))
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

    // endregion

    private data class SourceLayout(
        val srcPath: String,
        val srcResourcePath: String,
        val assetsPath: String,
        val testPath: String,
        val testResourcePath: String,
    )

    private fun resolveLayout(): SourceLayout {
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