rootProject.name = "dev.scaffoldit"

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
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
