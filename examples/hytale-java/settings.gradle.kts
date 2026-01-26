rootProject.name = "dev.scaffoldit.examples.java"

pluginManagement {
    plugins {
        id("dev.scaffoldit") version "0.1.7-dev"
        id("com.gradleup.shadow") version "9.3.1"
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.scaffoldit")
}

hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.JavaExample"
    }
}