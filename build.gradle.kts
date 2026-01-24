plugins {
    kotlin("jvm") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    `java-gradle-plugin`
}

allprojects {
    group = "dev.scaffoldit"
    version = "0.1.4-dev"
}

subprojects {
    apply(plugin = "com.vanniktech.maven.publish")

    afterEvaluate {
        val pkgName = project.extra["packageName"] as String? ?: project.name
        val pkgDesc = project.description ?: project.name
        mavenPublishing {
            publishToMavenCentral()
            signAllPublications()
            pom {
                name.set(pkgName)
                description.set(pkgDesc)
                url.set("https://github.com/adaliszk/scaffoldit")

                licenses {
                    license {
                        name.set("BSD-3-Clause")
                        url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit/blob/dev/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("adaliszk")
                        name.set("Ádám \"Kicsivazz\" Liszkai")
                        url.set("https://github.com/adaliszk")
                    }
                }

                scm {
                    url.set("https://github.com/adaliszk/scaffoldit")
                    connection.set("scm:git:git://github.com/adaliszk/scaffoldit.git")
                    developerConnection.set("scm:git:ssh://git@github.com/adaliszk/scaffoldit.git")
                }
            }
        }
    }
}

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

gradlePlugin {
    plugins {
        register("mavenCentral") {
            id = "dev.scaffoldit"
            implementationClass = "dev.scaffoldit.GradlePlugin"
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        name.set("ScaffoldIt")
        description.set("Gradle plugin and framework for cross-platform game mod development")
        url.set("https://github.com/adaliszk/scaffoldit")

        licenses {
            license {
                name.set("BSD-3-Clause")
                url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit/blob/dev/LICENSE.md")
            }
        }

        developers {
            developer {
                id.set("adaliszk")
                name.set("Ádám \"Kicsivazz\" Liszkai")
                url.set("https://github.com/adaliszk")
            }
        }

        scm {
            url.set("https://github.com/adaliszk/scaffoldit")
            connection.set("scm:git:git://github.com/adaliszk/scaffoldit.git")
            developerConnection.set("scm:git:ssh://git@github.com/adaliszk/scaffoldit.git")
        }
    }
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