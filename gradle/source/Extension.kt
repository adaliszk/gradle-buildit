// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

data class Extension(
    val setting: Class<*>,
    val project: Class<*>
)