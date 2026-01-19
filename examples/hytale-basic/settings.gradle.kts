
pluginManagement {
    includeBuild("../..")  // Points to the root plugin project
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
        kotlin("jvm") version "2.3.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "example"

plugins {
    id("dev.buildit.repository")
}

hytale {
    includeAssetPack = true
}