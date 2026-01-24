> [!WARNING]  
> In active development and experimentation, almost nothing is stable!

# ScaffoldIt! Gradle Plugin for Mod Development

Keep the necessary boilerplate at the absolute minimum while also having access to a robust and
flexible framework for mod development for Hytale, _later for Minecraft._

### Features

<!-- - Automatic scaffolding based on settings and annotations -->
- Monorepo support through common "multiloader" and "library" patterns
- Mixed Kotlin and Java projects with automatic dependencies
<!-- - Source provider for decompiling or using platforms such as Hytale -->
- Delegation-based wiring for cross-platform mod development
- Hot-swapping code and resources, including live reloading
<!-- - IDEA linter with compiler errors for fast feedback -->

_and more coming soon_

### Requirements

- IntelliJ IDEA (even Community edition), VS Code, or any other editor
- Java 25, use JetBrains Runtime (JBR) for hot swapping to work!
- Hytale installed through the launcher when used as the target

## Usage

```kotlin
// settings.gradle.kts
rootProject.name = "dev.example"
pluginManagement {
    plugins {
        id("dev.scaffoldit.modkit") version "0.1.1-dev"
        id("com.gradleup.shadow") version "9.3.1"
    }
    repositories {
        mavenCentral()
    }
}
plugins {
    id("dev.scaffoldit.modkit")
}
hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        IncludeAssetPack = true
    }
}
```

### `hytale { }`

Configures a hytale project with dependencies, manifest management, and inherits common packages
when they are declared. <!-- It automatically creates the necessary folder structure and even 
creates an example main class for you. -->

Options:

- `useKotlin(dependencyString)`: Use and specify the Kotlin STDLib for compilation.
- `useFlat()`: Switch the directory layout to flat that drops `main/com/example/project`.
- `manifest {}`: Configure plugin manifest details by their values.

### `common { }`

Collects and configures common library packages and adds them to the target projects as
dependencies. <!-- Similarly to `hytale` it automatically creates the necessary folder structure,
but will leave the main class up to you. -->

Options:

- `useKotlin(dependencyString)`: Use and specify the Kotlin STDLib for compilation.
- `useFlat()`: Switch the directory layout to flat that drops `main/com/example/project`.
- `include(...Strings) { }`: Include sub-package directory in the common configuration.

### Hot Swapping

To enable dynamic class reloading at runtime, you MUST use a JetBrains JRE, which is available for
free at https://github.com/JetBrains/JetBrainsRuntime, or you can manage it within the IDEA SDK
window. Once configured, update your run configuraitons to use it and watch for the Gradle widget
in your code section's top right corner.

<!-- The plugin also adds its own runtime agent to reload your mod where that is possible. -->

## Contributions

Feel free to open issues or pull requests if you have some problems, you can also reach out to me
on discord under the nickname of `kicsivazz`.
