package dev.scaffoldit.gradle

data class Extension(
    val setting: Class<*>,
    val project: Class<*>
)