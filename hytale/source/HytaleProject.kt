// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale

import dev.scaffoldit.api.ScaffoldIt
import dev.scaffoldit.api.Wired
import dev.scaffoldit.gradle.tasks.*
import dev.scaffoldit.gradle.Gradle
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Configure a Hytale project with:
 * - Standard Toolchain, Language, and Source Paths
 * - Testing Engine using Kotest or pure JUnit with Coverage support
 * - Automatically resolved HytaleServer dependency
 * - IDEA development server run configuration
 */
open class HytaleProject(private val project: Project) :
  Gradle.ConfigureToolchain by ToolchainManager(),
  Gradle.ConfigurePaths by SourceManager(),
  Gradle.ConfigureTests by TestingEngine(),
  Gradle.ConfigurePlatform by HytaleServerPlatform(),
  Gradle.ConfigureIdeaDev by HytaleDevserverRun(),
  Wired by ScaffoldIt() {

  private val log: Logger = Logging.getLogger(this::class.java)

  init {
    wire(this)
    log.lifecycle("> Plug :hytale(project):initialize()")
    with(ToolchainManager::class).configure(project)
    with(SourceManager::class).configure(project)
    with(TestingEngine::class).configure(project)
    with(HytaleServerPlatform::class).configure(project)
    with(HytaleDevserverRun::class).configure(project)
  }

  fun manifest(config: HytaleManifest.() -> Unit) {
    log.lifecycle("> Plug :hytale(project):manifest()")
    HytaleManifest.from(project).apply(config).configure(project)
    HytaleManifest.from(project).saveTo(project)
  }
}