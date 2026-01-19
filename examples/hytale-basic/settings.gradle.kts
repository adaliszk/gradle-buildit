
pluginManagement {
    includeBuild("../..") // <- Simulating JetPack
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "example"

plugins {
    id("com.github.adaliszk.gradle-buildit")
}

hytale {
    includeAssetPack()
}