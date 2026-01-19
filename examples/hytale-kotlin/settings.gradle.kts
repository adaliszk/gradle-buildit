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

plugins {
    id("com.github.adaliszk.gradle-buildit")
}

buildit {
    useKotlin()
}

hytale {
    includeAssetPack()
}