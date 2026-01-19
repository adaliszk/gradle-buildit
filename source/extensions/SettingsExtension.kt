package dev.buildit.gradle

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File


/**
 * @internal
 * Shared base extension capabilities based on the Setting itself to register projects,
 * and auto-configure their:
 * - plugin repositories
 * - maven repositories
 * - language dependencies
 * - source and resource paths
 * - tests tasks
 */
abstract class GradleExtension(protected val settings: Settings) : GradleExtension
{
    companion object
    {
        /**
         * Store the library projects that are automatically added to the main ones
         */
        val libs = mutableListOf<String>()
    }

    protected val project: Project by lazy {
        settings.gradle.rootProject
    }

    protected val plugin: SettingsExtension
        get() = settings.extensions.getByType(SettingsExtension::class.java)

    fun configureProject(target: Project)
    {
        if (target.subprojects.isNotEmpty()) return
        bootstrap(target)
    }

    open fun onConfigure(target: Project)
    {
        // Nothing to do here, but can be overridden!
    }

    open fun configureSettings()
    {
        // Nothing to do here, but can be overridden!
    }

    protected fun register(path: String, setupPreset: Project.() -> Unit = {})
    {
        register(
            path,
            ProjectMetadata(
                type = ProjectMetadata.Type.LIBRARY,
                packageName = path.substringAfterLast(':'),
            ),
            setupPreset,
        )
    }

    protected fun register(path: String, metadata: ProjectMetadata, setupPreset: Project.() -> Unit = {})
    {
        bootstrap(path)
        settings.include(path)
        settings.gradle.beforeProject { target ->
            if (target.path == path)
            {
                target.setupPreset()
                bootstrap(target)
            }
        }
    }

    private fun bootstrap(path: String)
    {
        File(settings.rootDir, path.replace(":", File.separator)).mkdirs()
        // TODO: Add bootstrapping for a quick start
    }

    private fun bootstrap(target: Project)
    {
        metadata.update(target)
        target.repositories.apply {
            mavenCentral()
            mavenLocal()
        }
        target.pluginManager.apply("java-library")
        configureKotlin(target)
        configureSourceSets(target)
        configureTests(target)
    }

    protected open fun configureKotlin(target: Project)
    {
        if (plugin.kotlinLibrary.isEmpty()) return

        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        target.dependencies.add(
            "implementation", plugin.kotlinLibrary,
        )
    }

    protected open fun configureSourceSets(target: Project)
    {
        target.extensions.findByName("sourceSets")?.let { sourceSets ->
            (sourceSets as SourceSetContainer).apply {
                named("main") {
                    it.java.setSrcDirs(listOf(plugin.sourceDir))
                    it.resources.setSrcDirs(listOf(plugin.resourcesDir))
                    target.file(plugin.sourceDir).mkdirs()
                    target.file(plugin.resourcesDir).mkdirs()
                }
                findByName("shared") ?: create("shared") {
                    it.resources.setSrcDirs(listOf(project.file("resources")))
                    project.file("resources").mkdirs()
                }
                named("test") {
                    it.java.setSrcDirs(listOf(plugin.testsDir))
                    target.file(plugin.testsDir).mkdirs()
                }
            }
        }

        target.plugins.withId("org.jetbrains.kotlin.jvm") {
            target.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets.apply {
                named("main").configure {
                    it.kotlin.srcDir(plugin.sourceDir)
                }
                findByName("shared")?.apply {
                    resources.setSrcDirs(listOf(project.file("resources")))
                }
                named("test").configure {
                    it.kotlin.srcDir(plugin.testsDir)
                }
            }
        }
    }

    protected open fun configureTests(target: Project)
    {
        target.plugins.withId("java") {
            target.dependencies.add(
                "testImplementation",
                "org.junit.jupiter:junit-jupiter:5.11.4",
            )
            target.tasks.named("test", Test::class.java) { task ->
                task.useJUnitPlatform()
                task.filter { f ->
                    f.includeTestsMatching("*Test")
                    f.isFailOnNoMatchingTests = false
                }
            }
        }
    }
}