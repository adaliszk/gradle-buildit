// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

import dev.scaffoldit.api.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging as GradleLogging

class GradleConsole : Logging<Logger> {
    override var log: Logger = GradleLogging.getLogger(this::class.java)
}
