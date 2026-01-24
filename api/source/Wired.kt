package dev.scaffoldit.api

import kotlin.reflect.KClass

interface Wired {
    fun wire(parent: Any)
    fun <T : Trait> with(cls: KClass<T>): T
    fun <T : Trait> with(cls: KClass<T>, trait: T.() -> Unit = {}): T
    fun <T : Trait> with(cls: Class<T>): T
    fun <T : Trait> with(cls: Class<T>, trait: T.() -> Unit = {}): T
}