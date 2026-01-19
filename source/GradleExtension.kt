package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File

/**
 * Shared base extension capabilities based on the Setting itself to register projects
 * and autoconfigure their:
 * - Plugin repositories
 * - Maven repositories
 * - Language dependencies
 * - Source and resource paths
 * - Tests tasks
 *
 * @internal
 */
abstract class GradleExtension(protected val pending: Lazy<Project>) {
    private val log: Logger = Logging.getLogger(GradleExtension::class.java)

    protected val project: Project by pending
    protected val config = BuildItExtension.Companion

    companion object {
        /** Stores the library projects that are automatically added to the target ones */
        val libs = mutableListOf<String>()
    }

    /**
     * Hook that gets called after the project is ready no matter if settings or build script
     */
    fun initialize() {
        prepareWorkspace()
        onInitialize()
    }

    /**
     * Override this to apply your extension's initialization steps
     */
    open fun onInitialize() {
        // Override this for custom logic
    }

    /**
     * Register a project path to the configurations, useful for nesting include() while also
     * allowing the users to expand them and provide any details.
     *
     * @internal
     */
    fun register(path: String, userConfiguration: Project.() -> Unit = {}) {
        if (libs.isEmpty()) return configure(userConfiguration)
        project.gradle.settingsEvaluated { settings ->
            prepareWorkspace()
            settings.include(path)
            configure(userConfiguration)
        }
    }

    /**
     * Configures the project with common settings and configurations while also allowing the users
     * to expand and provide their own settings that may overwrite the automatic ones.
     *
     * @internal
     */
    fun configure(userConfiguration: Project.() -> Unit = {}) {
        registerRepositories()
        applyToolchain()
        configureSourceSets()
        configureTests()
        userConfiguration(project)
    }

    private fun prepareWorkspace() {
        // Skip if no new directories possible to make
        if (!project.file(".").mkdirs()) return
        project.copy {
            // TODO: apply bootstrap template
        }
    }

    /** @internal */
    val mainClass by lazy {
        listOfNotNull(
            config("env.maven.group"),
            config("project.name"),
            config("env.maven.name").toTitlecase() + "Plugin"
        ).joinToString(".")
    }

    /** @internal */
    val mainFile: File? by lazy {
        val classPath = mainClass.replace(".", File.separator)
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName("main")

        mainSourceSet.allSource.firstOrNull { file ->
            file.absolutePath.endsWith("$classPath.kt") ||
                file.absolutePath.endsWith("$classPath.java")
        }
    }

    private fun registerRepositories() {
        project.repositories.apply {
            gradlePluginPortal()
            mavenCentral()
            mavenLocal()
        }
    }

    private fun applyToolchain() {
        with(project.pluginManager) {
            apply("java-library")
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
        }
        if (config.kotlinLibrary.isNotBlank()) {
            log.lifecycle("> Plug :applyToolchain() <- useKotlin(${config.kotlinLibrary})")
            project.pluginManager.apply("org.jetbrains.kotlin.jvm")
            project.dependencies.add("implementation", config.kotlinLibrary)
        }
    }

    private fun configureSourceSets() {
        // TODO: Figure out a nicer way
        with(project.extensions.getByType(SourceSetContainer::class.java)) {
            named("main") { main ->
                main.resources.setSrcDirs(listOf(config.resourcesDir))
                main.java.setSrcDirs(listOf(config.sourceDir))
            }
            findByName("assets") ?: create("assets") { assets ->
                assets.resources.setSrcDirs(listOf(project.file(config.assetDir)))
            }
            named("test") { test ->
                test.java.setSrcDirs(listOf(config.testsDir))
            }
        }

        if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            try {
                with(project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets) {
                    named("main") { main ->
                        main.resources.setSrcDirs(listOf(config.resourcesDir))
                        main.kotlin.setSrcDirs(listOf(config.sourceDir))
                    }
                    findByName("assets") ?: create("assets") { assets ->
                        assets.resources.setSrcDirs(listOf(project.file(config.assetDir)))
                    }
                    named("test") { test ->
                        test.kotlin.setSrcDirs(listOf(config.testsDir))
                    }
                }
            } catch (e: NoClassDefFoundError) {
                // Kotlin plugin API not available, skip Kotlin source set configuration
            }
        }
    }

    private fun configureTests() {
        project.pluginManager.apply("org.gradle.jacoco")
        project.dependencies.apply {
            if (config.kotlinLibrary.isNotBlank()) {
                add("testImplementation", "io.kotest:kotest-runner-junit5:5.8.0")
                add("testImplementation", "io.kotest:kotest-assertions-core:5.8.0")
                add("testImplementation", "io.kotest:kotest-property:5.8.0")
                return
            }
            add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
            add("testImplementation", "org.assertj:assertj-core:3.24.2")
        }
        project.tasks.named("test", Test::class.java) { test ->
            test.useJUnitPlatform()
            test.filter { f ->
                f.includeTestsMatching("*Test")
                f.includeTestsMatching("*Spec")
                f.isFailOnNoMatchingTests = false
            }
            test.finalizedBy("jacocoTestReport")
        }
        project.tasks.named("jacocoTestReport", JacocoReport::class.java) { report ->
            report.dependsOn("test")
            report.reports {
                it.xml.required.set(true)
            }
        }
    }

    protected fun config(property: String, default: String = ""): String {
        return project.providers.gradleProperty(property).getOrElse(default)
    }

    protected fun String.toTitlecase() = replaceFirstChar { it.uppercase() }
}