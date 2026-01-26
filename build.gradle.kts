plugins {
    kotlin("jvm") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    `java-gradle-plugin`
}

allprojects {
    group = "dev.scaffoldit"
    version = "0.1.7-dev"
}

subprojects {
    apply(plugin = "com.vanniktech.maven.publish")

    afterEvaluate {
        val pkgName = project.extra["packageName"] as String? ?: project.name
        val pkgDesc = project.description ?: project.name
        mavenPublishing {
            publishToMavenCentral()
            if (project.findProperty("signing.keyId") != null) {
                signAllPublications()
            }
            pom {
                name.set(pkgName)
                description.set(pkgDesc)
                url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit/blob/dev/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("adaliszk")
                        name.set("\"Kicsivazz\" Ádán Liszkai")
                        url.set("https://github.com/adaliszk")
                    }
                }

                scm {
                    url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit")
                    connection.set("scm:git:git://github.com/adaliszk/scaffoldit.git")
                    developerConnection.set("scm:git:ssh://git@github.com/adaliszk/gradle-scaffoldit-modkit.git")
                }
            }
        }
    }
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":gradle"))
    compileOnly(project(":devtools"))
    runtimeOnly(project(":devtools"))
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
    if (project.findProperty("signing.keyId") != null) {
        signAllPublications()
    }
    pom {
        name.set("ScaffoldIt")
        description.set("Gradle plugin and framework for cross-platform game mod development")
        url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit")

        licenses {
            license {
                name.set("MIT")
                url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit/blob/dev/LICENSE.md")
            }
        }

        developers {
            developer {
                id.set("adaliszk")
                name.set("\"Kicsivazz\" Ádán Liszkai")
                url.set("https://github.com/adaliszk")
            }
        }

        scm {
            url.set("https://github.com/adaliszk/gradle-scaffoldit-modkit")
            connection.set("scm:git:git://github.com/adaliszk/gradle-scaffoldit-modkit.git")
            developerConnection.set("scm:git:ssh://git@github.com/adaliszk/gradle-scaffoldit-modkit.git")
        }
    }
}

sourceSets {
    main {
        kotlin.setSrcDirs(listOf("plugin"))
        java.setSrcDirs(listOf("plugin"))
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
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}