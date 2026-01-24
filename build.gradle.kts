plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    `java-gradle-plugin`
    `maven-publish`
}

project.group = "dev.scaffoldit"
project.version = System.getenv("GITHUB_REF_NAME") ?: "0.0.0-dev"

repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":gradle"))
    implementation(project(":common"))
    implementation(project(":hytale"))
}

//gradlePlugin {
//    plugins {
//        register("plugin") {
//            id = "com.github.adaliszk.gradle-scaffoldit-modkit"
//            implementationClass = "dev.scaffoldit.GradlePlugin"
//        }
//    }
//}
//
//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            from(components["java"])
//        }
//    }
//}

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