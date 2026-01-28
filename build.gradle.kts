plugins {
    kotlin("jvm") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("io.kotest") version "6.1.1"
    id("com.google.devtools.ksp") version "2.3.4"
    `java-gradle-plugin`
}

allprojects {
    group = "dev.scaffoldit"
    version = "0.2.2"
}

subprojects {
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.matching { it.name == "sourcesJar" }.configureEach {
        (this as Jar).mustRunAfter(tasks.matching { it.name == "compileTestJava" })
    }

    afterEvaluate {
        val x = extensions.extraProperties
        val pkgName = if (x.has("packageName")) x["packageName"].toString() else name
        val pkgDesc = description ?: name

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
    implementation(project(":devtools"))
    implementation(project(":common"))
    implementation(project(":hytale"))
    testImplementation("io.kotest:kotest-runner-junit5:6.1.1")
    testImplementation("io.kotest:kotest-assertions-core:6.1.1")
    testImplementation("io.kotest:kotest-property:6.1.1")
    testImplementation("io.github.serpro69:kotlin-faker:1.15.0")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("mavenCentral") {
            implementationClass = "dev.scaffoldit.GradlePlugin"
            id = "dev.scaffoldit"
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
        kotlin.setSrcDirs(listOf("plugin/source"))
        java.setSrcDirs(listOf("plugin/source"))
        resources.setSrcDirs(listOf("plugin/resources"))
    }
    test {
        kotlin.setSrcDirs(listOf("plugin/tests"))
        java.setSrcDirs(listOf("plugin/tests"))
        resources.srcDir(tasks.pluginUnderTestMetadata.map { it.outputDirectory })
    }
}

kotlin {
    jvmToolchain(25)
    sourceSets {
        test {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:6.1.1")
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}