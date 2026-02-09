extra["packageName"] = "ScaffoldIt Common Gradle"
description =
    "Registers project and setting extensions for common Gradle scopes where libraries of mods live."

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Gradle API for the plugin classpath
    compileOnly(gradleApi())
    // Kotlin Gradle Plugin to reference Kotlin DSL classes
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("stdlib"))
    // Monorepo
    implementation(project(":api"))
    implementation(project(":gradle"))
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

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}