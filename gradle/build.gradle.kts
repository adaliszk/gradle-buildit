plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    `java-gradle-plugin`
}

extra["packageName"] = "ScaffoldIt Gradle"
description = "Wires Gradle tasks and the plugin with ScaffoldIt."

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
    // Libraries to simplify the implementation
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.3")
    // Monorepo
    implementation(project(":api"))
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
    options.release.set(24)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
    }
}