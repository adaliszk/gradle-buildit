package dev.buildit.gradle

import org.gradle.api.Project


internal interface GradleExtension
{
    fun configureSettings() {}
    fun configureProject(target: Project) {}
}