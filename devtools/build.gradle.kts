extra["packageName"] = "ScaffoldIt Devtools"
description = "Provides a runtime plugin that instruments debug JRE with Hot Swapping."

plugins {
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.18")
    compileOnly("com.hypixel.hytale:Server:+")
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
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}