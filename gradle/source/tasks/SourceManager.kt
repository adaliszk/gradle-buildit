package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.Language
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.extensions.core.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class SourceManager : Gradle.ConfigurePaths {
    private val log: Logger = Logging.getLogger(this::class.java)
    private val cls: String = "${this::class.simpleName}[${System.identityHashCode(this)}]"

    override var projectDir: String = ""

    var packageDir: String = "" // TODO: Automatically initialize this
    lateinit var project: Project

    override fun useFlat() {
        log.lifecycle("> Set $cls:useFlat()")
        flatLayout = true
        configure(project)
    }

    var flatLayout: Boolean = false
    var language: Language = Language.JAVA
    val sourceDir: String
        get() = "src/main/${language.dir}"
    var testsDir: String = "src/main/tests"
    var resourceDir: String = "src/main/resources"
    var assetsDir: String = "resources"

    fun configure(project: Project): SourceManager {
        log.lifecycle("> Plug :${project.name}:configure(project) by $cls")
        this.language = if (project.extra["kotlin"] != null) Language.KOTLIN else Language.JAVA
        this.project = project

        val layout = resolveLayout()
        log.lifecycle("> Source :$language(flat=$flatLayout) in ${project.file(layout.srcPath)}")

        fun <T : Any> NamedDomainObjectContainer<T>.configureSourceDirs(
            resources: T.() -> SourceDirectorySet,
            source: T.() -> SourceDirectorySet
        ) {
            named("main") { main ->
                main.resources().setSrcDirs(listOf(project.file(layout.resourcePath)))
                main.source().setSrcDirs(listOf(project.file(layout.srcPath)))
            }
            findByName("assets") ?: create("assets") { assets ->
                assets.resources().setSrcDirs(listOf(project.file(layout.assetsPath)))
            }
            named("test") { test ->
                test.resources().setSrcDirs(listOf(project.file(layout.resourcePath)))
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

    data class SourceLayout(
        val srcPath: String,
        val resourcePath: String,
        val assetsPath: String,
        val testPath: String
    )

    fun resolveLayout(): SourceLayout {
        return when (flatLayout) {
            true -> SourceLayout(
                srcPath = "$projectDir/source",
                resourcePath = "$projectDir/resources",
                assetsPath = "/$assetsDir",
                testPath = "$projectDir/source",
            )
            else -> SourceLayout(
                srcPath = "$projectDir/$sourceDir/$packageDir",
                resourcePath = "$projectDir/$resourceDir",
                assetsPath = "/$assetsDir",
                testPath = "$projectDir/$testsDir",
            )
        }
    }
}