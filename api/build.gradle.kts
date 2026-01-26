extra["packageName"] = "ScaffoldIt API"
description =
  "Provides interfaces and generic handlers for wiring components and systems via delegation."

plugins {
  kotlin("jvm") version "2.3.0"
}

repositories {
  mavenLocal()
  mavenCentral()
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

val generateBuildConfig by tasks.registering {
  val outputDir = layout.buildDirectory.dir("generated")
  outputs.dir(outputDir)
  doLast {
    val file = outputDir.get().file("VERSION.kt").asFile
    file.parentFile.mkdirs()
    file.writeText(
      """
            // Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT
            
            package dev.scaffoldit.api
            
            val VERSION = "${project.version}"
        """.trimIndent()
    )
  }
}

kotlin {
  jvmToolchain(25)
  sourceSets.main {
    kotlin.srcDir(generateBuildConfig)
  }
}

tasks.compileKotlin {
  dependsOn(generateBuildConfig)
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
  }
}