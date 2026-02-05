// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.hytale.wire

@Suppress("PropertyName") // We mimic the format of the hytale manifest here
data class DevServerConfig(
    var Enabled: Boolean = true,
    var AllowOp: Boolean = true,
    var OfflineMode: Boolean = true,
    var DisableSentry: Boolean = true,
    var AcceptEarlyPlugins: Boolean = false,
    var IncludeUserMods: Boolean = false,
) {
    fun toArgs() = mapOf(
        ::AllowOp to "--allow-op",
        ::DisableSentry to "--disable-sentry",
        ::AcceptEarlyPlugins to "--accept-early-plugins",
        ::OfflineMode to "--auth-mode=offline",
    ).filter { it.key.get() }
        .values.toList()
}