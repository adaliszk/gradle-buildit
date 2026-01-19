rootProject.name = "dev.buildit"

pluginManagement {
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