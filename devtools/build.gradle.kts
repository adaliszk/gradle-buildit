plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
}

extra["packageName"] = "ScaffoldIt Devtools"
description = "Provides a runtime plugin that instruments debug JRE with Hot Swapping."


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
    compileOnly("com.hypixel.hytale:Server:0.5.2")
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