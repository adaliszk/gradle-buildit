package dev.scaffoldit.gradle.tasks

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.gradle.Language
import dev.scaffoldit.gradle.hytale.HytaleConfig
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.extensions.core.extra
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.IdeaExtPlugin
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class HytaleDevserverRun : Gradle.ConfigureIdeaDev {
    private val log: Logger = Logging.getLogger(this::class.java)

    fun configure(project: Project): Gradle.ConfigureIdeaDev {
        log.debug(":${project.name}:HytaleDevserverRun(project)")

        val hytale = HytaleConfig(project)

        hytale.validateHytaleInstallation()

        val serverRunDir = project.file("devserver")
        if (serverRunDir.mkdirs()) {
            javaClass.getResourceAsStream("/hytale/server.zip")?.use { stream ->
                project.zipTree(project.file("temp.zip").apply { writeBytes(stream.readBytes()) })
                    .let { tree -> project.copy { it.from(tree); it.into(serverRunDir) } }
                    .also { project.delete("temp.zip") }
            }
        }

        val rootProject = project.gradle.rootProject

        fun createServerRunArguments(): String {
            val assetsPath =
                "${hytale.homePath}/install/${hytale.patchline}/package/game/latest/Assets.zip"
            var params =
                "--allow-op --disable-sentry --accept-early-plugins --assets=\"$assetsPath\""
            val modPaths = mutableListOf<String>()
            val language = if (project.extra["kotlin"] != null) Language.KOTLIN else Language.JAVA
            val srcDir = when (language) {
                Language.KOTLIN -> project.extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
                    .getByName("main").kotlin.srcDirs.first().parentFile.absolutePath

                Language.JAVA -> project.extensions.getByType(SourceSetContainer::class.java)
                    .getByName("main").java.srcDirs.first().parentFile.absolutePath
            }
            modPaths.add(srcDir)
            // TODO: Parse hytale config argument
            // if (loadUserMods) {
            //    modPaths.add("${hytale.homePath}/UserData/Mods")
            //}
            params += " --mods=\"${modPaths.joinToString(",")}\""
            return params
        }

        project.afterEvaluate {
            rootProject.plugins.apply(IdeaExtPlugin::class.java)
            rootProject.extensions.configure(IdeaModel::class.java) { idea ->
                val ideaProject = idea.project ?: return@configure
                (ideaProject as ExtensionAware).extensions.configure(ProjectSettings::class.java) { settings ->
                    settings.runConfigurations.create(
                        "${project.name}:devserver",
                        Application::class.java
                    ) { config ->
                        config.mainClass = "com.hypixel.hytale.Main"
                        config.moduleName = "${hytale.mainPackage}.main".removePrefix(".")
                        config.programParameters = createServerRunArguments()
                        config.workingDirectory = serverRunDir.absolutePath
                        // config.workingDirectory = project.layout.buildDirectory.dir("libs").get().asFile.absolutePath
                        config.jvmArgs = "-XX:+AllowEnhancedClassRedefinition"
                    }
                }
            }
        }

        return this
    }
}