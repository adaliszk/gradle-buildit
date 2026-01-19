package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

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
    protected val project: Project by pending

    companion object {
        /** Stores the library projects that are automatically added to the target ones */
        val libs = mutableListOf<String>()
    }

    fun initialize() {
        prepareWorkspace()
        onInitialize()
    }

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
        if(libs.isEmpty()) return configure(userConfiguration)
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

    private fun registerRepositories() {
        project.repositories.apply {
            mavenCentral()
            mavenLocal()
        }
    }

    private val config = BuildItExtension

    private fun applyToolchain() {
        with(project.pluginManager) {
            apply("java-library")
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
            if (config.kotlinLibrary.isNotBlank()) {
                apply("org.jetbrains.kotlin")
            }
        }
        with(project.dependencies) {
            if (config.kotlinLibrary.isNotBlank()) {
                add("implementation", config.kotlinLibrary)
            }
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
        project.plugins.findPlugin("org.jetbrains.kotlin.jvm")?.let {
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
}