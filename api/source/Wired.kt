// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.api

import kotlin.reflect.KClass

interface Wired {
    var parent: Wired
    fun wire(parent: Wired)
    fun <T : Trait> with(cls: KClass<T>): T
    fun <T : Trait> with(cls: KClass<T>, trait: T.() -> Unit = {}): T
    fun <T : Trait> with(cls: Class<T>): T
    fun <T : Trait> with(cls: Class<T>, trait: T.() -> Unit = {}): T
}