extra["packageName"] = "ScaffoldIt Hytale Gradle"
description = "Registers project and setting extensions for hytale Gradle scopes where the actual mods are implemented."

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3" apply false
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    // Gradle API for the plugin classpath
    compileOnly(gradleApi())
    // Kotlin Gradle Plugin to reference Kotlin DSL classes
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("stdlib"))
    // Libraries
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.3")
    compileOnly("com.hypixel.hytale:Server:+")
    // Monorepo
    implementation(project(":api"))
    implementation(project(":gradle"))
}

configurations {
    apiElements {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.jar)
    }
    runtimeElements {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.jar)
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
    relocate("org.jetbrains.gradle.plugin.idea", "dev.scaffoldit.shaded.idea")
}

sourceSets {
    main {
        kotlin.setSrcDirs(listOf("source"))
        java.setSrcDirs(listOf("source"))
        resources.setSrcDirs(listOf("resources"))
    }
    test {
        kotlin.setSrcDirs(listOf("test"))
        java.setSrcDirs(listOf("test"))
    }
}

kotlin {
    jvmToolchain(25)
}

//
// We explicitly set the JVM target to 24 which is the highest supported by Kotlin 2.3.0
// TODO: Remove the JVM 24 overwrites once Kotlin supports 25!
//

tasks.withType<JavaCompile>().configureEach {
    options.release.set(24)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
    }
}