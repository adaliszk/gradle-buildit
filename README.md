# ScaffoldIt! Gradle Plugin

Keep the necessary boilerplate at the absolute minimum while also having access to a robust and
flexible framework for mod development for Hytale, _later for Minecraft._

### Features

- Hytale server dependencies using the official hytale maven
- Monorepo support through common "multiloader" and "library" patterns
- Mixed Kotlin and Java projects with automatic dependencies
- Delegation-based wiring for cross-platform mod development
- Hot-swapping code and resources, including live reloading

<!-- - Source provider for decompiling or using platforms such as Hytale -->
<!-- - IDEA linter with compiler errors for fast feedback -->

### Requirements

- IntelliJ IDEA (even Community edition), VS Code, or any other editor
- Java 25, use JetBrains Runtime (JBR) for hot swapping to work!
- Hytale installed through the launcher when used as the target

### Usage

```kotlin
// settings.gradle.kts
rootProject.name = "dev.example"
pluginManagement {
    plugins {
        id("dev.scaffoldit") version "0.1.5-dev"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("dev.scaffoldit")
}
hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.example.mod.ExamplePlugin"
        IncludeAssetPack = true
    }
}
// that's it, no `build.gradle.kts` needed!
```

### `hytale { }`

Configures a hytale project with dependencies, manifest management, and inherits common packages
when they are declared. <!-- It automatically creates the necessary folder structure and even 
creates an example main class for you. -->

Options:

<!-- - `include(...Strings) { }`: Include sub-package directory in the common configuration. -->
- `useKotlin(dependencyString)`: Use and specify the Kotlin STDLib for compilation.
- `useFlat()`: Switch the directory layout to flat that drops `main/com/example/project`.
- `manifest {}`: Configure plugin manifest details by their values.

### `common { }`

Collects and configures common library packages and adds them to the target projects as
dependencies. <!-- Similarly to `hytale` it automatically creates the necessary folder structure,
but will leave the main class up to you. -->

Options:

- `include(...Strings) { }`: Include sub-package directory in the common configuration.
- `useKotlin(dependencyString)`: Use and specify the Kotlin STDLib for compilation.
- `useFlat()`: Switch the directory layout to flat that drops `main/com/example/project`.

### Hot Swapping

To enable dynamic class reloading at runtime, you MUST use a JetBrains JRE, which is available for
free at https://github.com/JetBrains/JetBrainsRuntime, or you can manage it within the IDEA SDK
window. Once configured, update your run configuraitons to use it and watch for the Gradle widget
in your code section's top right corner.

There is also an Agent Plugin that at runtime detects the Hot Swapping and reloads plugins
automatically for you; with that you only need to click on "Code Changed" in the editor UI.

# ScaffoldIt! Modding API

> [!WARNING]  
> The Modding API is in active development, subject to change, and may have missing features!

To streamline delivering code, a tiny micro-framework is provided to _wire_ components using Kotlin
delegation. With it, you can write modular code that hides the low-level details for the most common
tasks but still exposes enough power to ship anything custom.

```kotlin
dependencies {
    implementation("dev.scaffoldit:api:0.1.5-dev")
}
```

```kotlin
// Instrument mod entrypoint
class ExamplePlugin(init: JavaPluginInit) : JavaPlugin(init),
    Wired by ScaffoldIt.Scoped()

// Auto-register a feature
class ExampleCommand : CommandBase,
    Wired.Command by Hytale.Command()
    Wired by ScaffoldIt.Scoped()
``` 

# Contributions

Feel free to open issues or pull requests if you have some problems, you can also reach out to me
on discord under the nickname of `kicsivazz`.
