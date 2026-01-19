rootProject.name = "dev.buildit"

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}