// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.gradle

data class ExtensionSet<A : Extension, B : Extension>(
    val setting: Class<A>,
    val project: Class<B>,
)