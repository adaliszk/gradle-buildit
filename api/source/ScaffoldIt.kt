package dev.scaffoldit.api

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

class ScaffoldIt : Wired {
    companion object {
        const val VERSION = "0.1.6-dev"
    }

    lateinit var parent: Any

    override fun wire(parent: Any) {
        this.parent = parent
    }

    override fun <T : Trait> with(cls: KClass<T>): T {
        val traitInterface = cls.allSuperclasses
            .plus(cls.allSupertypes.mapNotNull { it.classifier as? KClass<*> })
            .firstOrNull { it.isSubclassOf(Trait::class) }
        val propertyName = "_wired${traitInterface?.simpleName ?: cls.simpleName}"
        val property = this.parent::class.memberProperties.find { it.name.endsWith(propertyName) }
            ?: error("No delegated property $propertyName found")

        @Suppress("UNCHECKED_CAST")
        return property.getter.call(parent) as T
    }

    override fun <T : Trait> with(cls: KClass<T>, trait: T.() -> Unit): T {
        return with(cls).apply(trait)
    }

    override fun <T : Trait> with(cls: Class<T>): T {
        return with(cls.kotlin)
    }

    override fun <T : Trait> with(cls: Class<T>, trait: T.() -> Unit): T {
        return with(cls.kotlin, trait)
    }
}