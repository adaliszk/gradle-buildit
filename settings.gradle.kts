rootProject.name = "dev.scaffoldit"

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
        kotlin("jvm") version "2.3.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

include("api")
include("gradle")
include("common")
include("hytale")
