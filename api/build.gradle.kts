plugins {
    kotlin("jvm") version "2.3.0"
}

project.group = "dev.scaffoldit.api"
project.version = System.getenv("GITHUB_REF_NAME") ?: "0.0.0-dev"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Kotlin Gradle Plugin to reference Kotlin DSL classes
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
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