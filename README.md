# ScaffoldIt! Gradle Plugin

Keep the necessary boilerplate at the absolute minimum while also having access to a robust and
flexible framework for mod development for Hytale, _and later for Minecraft._

## Features

- MavenCentral and CurseMaven are automatically registered and ready to use
- Hytale server dependencies using the official HytaleMaven ready to use
- Monorepo support through common "multiloader" and "library" patterns
- Mixed Kotlin and Java projects with automatic dependencies
- Auto-create IDEA run configuration for JetBrains DCEVM hot swapping
- Hot-reload plugin (even with dependencies) via an agent using DCEVM
- Direct source linking (no copy-paste) with the devserver
- Configured to compile Sources and Javadocs for your publications

## Requirements

- IntelliJ IDEA (even Community edition), VS Code, or any other editor
- Java 25, use JetBrains Runtime (JBR) for hot swapping to work!
- Hytale installed through the launcher when used as the target

## Usage

```kotlin
// settings.gradle.kts
rootProject.name = "dev.example"
pluginManagement {
  plugins {
    id("dev.scaffoldit") version "0.1.7-dev"
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
    IncludesAssetPack = true
  }
}
// that's it, no build.gradle.kts needed (but the same can be done there)
```

### `common { }`

_Available both in `settings.gradle[.kts]` and `build.gradle[.kts]` scripts!_

> [!IMPORTANT]
> This is meant to be declared before platform-related blocks!

Collects and configures common library packages and adds them to the target projects as
dependencies. All `include()` projects are relative from the `common` directory. It automatically
creates the necessary folder structure but leaves the classes itself to you.

##### `projectDir: String`

Configures the base project directory, set to `common` here.

##### `repositories {}`, `dependencies {}`,  `include(...projectString)`, `include(projectString)`

Exposes the standard dependency management and project declaration where you can share the repos
and libraries with all projects and declare any number of groups with their shared repos and
dependencies, like:

```kotlin
common {
  dependencies {
    compileOnly("dev.example.project:package:version") // <- Shared with all includes()
  }
  include("one", "two", "three") {
    dependencies {
      compileOnly("dev.example.project:package:version") // <- Shared with the include group
    }
  }
  include("four") // <- one, two, three automatically added here!
}
```

### `hytale { }`

_Available both in `settings.gradle[.kts]` and `build.gradle[.kts]` scripts!_

Configures a hytale project with dependencies, manifest management, and inherits common packages
when they are declared. It automatically creates the necessary folder structure but leaves the
classes itself to you.

##### `repositories {}`, `dependencies {}`,  `include(...projectString)`, `include(projectString)`

Exposes the standard dependency management and project declaration where you can share the repos
and libraries with all projects and declare any number of groups with their shared repos and
dependencies, like:

```kotlin
hytale {
  dependencies {
    compileOnly("dev.example.project:package:version") // <- Shared with all includes()
  }
  include("one", "two", "three") {
    dependencies {
      compileOnly("dev.example.project:package:version") // <- Shared with the include group
    }
  }
  include("four") // <- one, two, three automatically added here!
}
```

##### `useKotlin(dependencyString)`

Use and specify the Kotlin STDLib for compilation, left alone it will import the standard
`org.jetbrains.kotlin:kotlin-stdlib` but you can specify your own version, such as anything already
distributed on the curse maven.

##### `useFlat()`

Switch the directory layout to flat that drops `main/com/example/project`, this is useful for Kotlin
as it reduces the complexity of the project structure and the compiler still re-creates that within
your built jar file.

##### `manifest { }`

- `Group: String`: Group identifier, typically the organization in one PascalCase word.
- `Name: String`: Plugin name, typically in one PascalCase word, displayed in the mods list.
- `Version: String`: Semantic version ("1.0.0"), displayed in the most list with the "v" prefix.
- `Description: String?`: Optional field, not shown anywhere for now.
- `Author: List<Author>?`: The .Name used to show in the mods list "by ..." section.
- `Website: String?`: Optional field, not shown anywhere for now.
- `DisabledByDefault: Boolean`: Controls if the plugin is loaded at startup or not.
- `IncludesAssetPack: Boolean`: Flags the plugin for loading and watching assets.
- `Dependencies: Map<Group:Name, VersionRange>?`: What needs to "SETUP" with your plugin.
- `OptionalDependencies: Map<Group:Name, VersionRange>?`: What needs exist but no matter what state.
- `LoadBefore: Map<Group:Name, VersionRange>?`: Moves your plugin to load before the first of these.
- `ServerVersion: VersionRange`: Version matcher, e.g., ">=1.0.0".
- `Main: String`: Your main class full name to be loaded from classpath.
- `SubPlugins: List<HytaleManifest>?`: Same manifest as above to configure any sub-plugins.

### Hot Swapping

To enable dynamic class reloading at runtime, you MUST use a JetBrains JRE, which is available for
free at https://github.com/JetBrains/JetBrainsRuntime, or you can manage it within the IDEA SDK
window. Once configured, update your run configuraitons to use it and watch for the Gradle widget
in your code section's top right corner.

Scaffoldit also ships Agent Plugin that at runtime detects the Hot Swapping and reloads plugins
automatically for you; with that you only need to click on "Code Changed" in the editor UI. If you
only need this agent, you can use it by adding:

```kotlin
dependencies {
  runtimeOnly("dev.scaffoldit:devtools:0.1.7-dev")
}
```

# Contributions

Feel free to open issues or pull requests if you have some problems, you can also reach out to me
on discord under the nickname of `kicsivazz`.
