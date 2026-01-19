package dev.buildit.gradle.extensions

open class BuilditExtension
{
    var sourceDir: String = "src/main/java"
    var resourcesDir: String = "src/main/resources"
    var testsDir: String = "src/test/java"

    var kotlinLibrary: String = ""

    fun useKotlin(dependencyNotation: String? = null)
    {
        kotlinLibrary = dependencyNotation ?: "org.jetbrains.kotlin:kotlin-stdlib"
        sourceDir = "src/main/kotlin"
        resourcesDir = "src/main/resources"
        testsDir = "src/test/kotlin"
    }

    fun useFlat()
    {
        TODO("Implement source abstraction handler")
    }
}