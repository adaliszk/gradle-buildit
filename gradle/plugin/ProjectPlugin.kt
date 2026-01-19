package dev.buildit.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

@Suppress("unused") // Used by Gradle, but that is not visible
class ProjectPlugin : Plugin<Project>
{
    override fun apply(project: Project)
    {
        configureKotlinToolchain(project)
        configureJavaToolchain(project)
        configureRepositories(project)
        configureDependencies(project)
    }

    private fun configureRepositories(project: Project)
    {
        project.repositories.apply {
            mavenCentral()
            mavenLocal()
        }
    }

    private fun configureDependencies(project: Project)
    {
        // TODO: Add useful dependencies here
    }

    private fun configureJavaToolchain(project: Project)
    {
        project.plugins.withId("java") {
            project.extensions.findByType(JavaPluginExtension::class.java)?.apply {
                toolchain.languageVersion.set(JavaLanguageVersion.of(24))
            }
        }
    }

    private fun configureKotlinToolchain(project: Project)
    {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            val kotlin = project.extensions.findByName("kotlin")
            kotlin?.javaClass?.getMethod("jvmToolchain", Int::class.java)
                ?.invoke(kotlin, 24)
        }
    }
}