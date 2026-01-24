rootProject.name = "example.kotlin"

pluginManagement {
    plugins {
        id("dev.scaffoldit") version "0.1.5-dev"
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
    useKotlin()
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.KotlinExample"
    }
}