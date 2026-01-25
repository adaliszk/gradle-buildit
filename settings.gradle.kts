rootProject.name = "dev.scaffoldit"

pluginManagement {
    plugins {
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.3" apply false
        kotlin("jvm") version "2.3.0"
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

include("api", "gradle", "devtools")
include("common", "hytale")
