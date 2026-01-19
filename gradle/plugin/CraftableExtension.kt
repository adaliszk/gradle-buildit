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


abstract class CraftableExtension(protected val settings: Settings) : GradleExtension
{
    private val log: Logger = Logging.getLogger(CraftableExtension::class.java)
    private val logCtx: String = "> Conf "

    protected val project: Project by lazy {
        settings.gradle.rootProject
    }

    protected val plugin: SettingsExtension
        get() = settings.extensions.getByType(SettingsExtension::class.java)

    companion object
    {
        val libs = mutableListOf<String>()
    }

    protected val sourceDir: String
        get() = plugin.sourceDir

    protected val resourcesDir: String
        get() = plugin.resourcesDir

    protected val testsDir: String
        get() = plugin.testsDir


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
        // Auto-generate registered project paths
        File(settings.rootDir, path.replace(":", File.separator)).mkdirs()
        // TODO: Add bootstrapping for a quick start

        settings.include(path)
        settings.gradle.beforeProject { target ->
            if (target.path == path)
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
                target.setupPreset()
            }
        }
    }

    open fun configure(target: Project)
    {
        // Nothing to do here, but can be overridden!
    }

    final override fun configureProject(target: Project)
    {
        if (target.subprojects.isNotEmpty()) return

        project.repositories.apply {
            mavenCentral()
            mavenLocal()
        }
        project.pluginManager.apply("java-library")
        configureKotlin(target)
        configureSourceSets(target)
        configureTests(target)
        configure(target)
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
                    it.java.setSrcDirs(listOf(sourceDir))
                    it.resources.setSrcDirs(listOf(resourcesDir))
                    target.file(sourceDir).mkdirs()
                    target.file(resourcesDir).mkdirs()
                }
                findByName("shared") ?: create("shared") {
                    it.resources.setSrcDirs(listOf(project.file("resources")))
                    project.file("resources").mkdirs()
                }
                named("test") {
                    it.java.setSrcDirs(listOf(testsDir))
                    target.file(testsDir).mkdirs()
                }
            }
        }

        target.plugins.withId("org.jetbrains.kotlin.jvm") {
            target.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets.apply {
                named("main").configure {
                    it.kotlin.srcDir(sourceDir)  // Add to existing, don't replace
                }
                findByName("shared")?.apply {
                    resources.setSrcDirs(listOf(project.file("resources")))
                }
                named("test").configure {
                    it.kotlin.srcDir(testsDir)  // Add to existing, don't replace
                }
            }
        }
    }

    private fun configureResourcesTask(target: Project)
    {
        val remapSources = target.tasks.register("remapSources", Copy::class.java) { task ->
            task.from(sourceDir)
            task.into(target.layout.buildDirectory.dir("generated-src/main"))

            task.eachFile { file ->
                val packagePath = "dev/buildit/${target.name}/${target.name}"
                file.path = "$packagePath/${file.path}"
            }
        }

//        target.afterEvaluate {
//            target.tasks.named("processResources", Copy::class.java) { task ->
//                task.dependsOn(updateManifest)
//                task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
//            }
//        }
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