// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale.wire

import dev.scaffoldit.gradle.Gradle
import dev.scaffoldit.hytale.HytaleExtension
import dev.scaffoldit.hytale.Patchline
import org.gradle.api.Action

object HytaleGradle {
    interface ConfigurePlatform : Gradle.ConfigurePlatform {
        var patchline: Patchline
        fun usePatchline(value: String) {
            this.patchline = Patchline.valueOf(value.uppercase().replace("-", "_"))
            HytaleExtension.patchline = this.patchline.repo
        }

        var version: String
        fun useVersion(version: String) {
            this.version = version.replace("latest", "+")
            HytaleExtension.version = this.version
        }

        fun manifest(config: HytaleManifest.() -> Unit)
        fun manifest(action: Action<HytaleManifest>)
    }

    interface ConfigureIdeaDev : Gradle.ConfigureIdeaDev {
        fun devserver(config: DevServerConfig.() -> Unit)
        fun devserver(action: Action<DevServerConfig>)
    }
}