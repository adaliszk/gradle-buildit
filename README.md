<div align="center">

# Scaffoldit Gradle Plugin

[![Java](https://img.shields.io/badge/Java-25_0-orange?style=for-the-badge)](https://openjdk.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-2_3-7F52FF?style=for-the-badge)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-9_2-1BA8CB?style=for-the-badge)](https://gradle.org)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<!--
[![Docs](https://img.shields.io/badge/Docs-blue?style=for-the-badge)](https://scaffoldit.dev)
[![Hytale](https://img.shields.io/badge/Hytale-blue?style=for-the-badge)](https://scaffoldit.dev)
[![Fabric](https://img.shields.io/badge/Fabric-blue?style=for-the-badge)](https://scaffoldit.dev)
[![NeoForge](https://img.shields.io/badge/NeoForge-blue?style=for-the-badge)](https://scaffoldit.dev)
-->

Keep the necessary boilerplate at the absolute minimum while also having access to a <br/>
and flexible toolkit for mod development for Hytale, _and later for Minecraft._

</div>

<table>
<tr>
  <td><b>Zero-boilerplate Gradle</b><br/> Configure everything from settings or build, kotlin or groovy.</td>
  <td><b>All toggles exposed</b><br/> Every argument, every version, is fully exposed for customization.</td>
  <td><b>Batteries included</b><br/> MavenCentral, CurseMaven, HytaleMaven auto-wired, ready to use!</td>
</tr>
<tr>
  <td><b>First-class Hytale support</b><br/> Fully typed manifest, nested SubPlugins, in-place generation.</td>
  <td><b>Agent-based hot-reload</b><br/> Runtime mod lifecycle reloads using `DCEVM` for code hot swapping.</td>
  <td><b>Monorepo & multiloader ready</b><br/> `common {}` + `hytale {}` = automatic workspace linking for multi-workspace use.</td>
</tr>
<tr>
  <td><b>Kotlin & Java seamlessly</b><br/> Java pre-configured, and `useKotlin()` available to switch to Kotlin!</td>
  <td><b>Flat layout support</b><br/> Skip `main/java/package` path via `useFlat()` without affecting jars.</td>
  <td><b>Gradle tasks</b><br/> Expose all IDE-style integrations as tasks for CLI usage.</td>
</tr>
<tr>
  <td><b>IDEA configuration</b><br/> Automatic run configuration detection and creation for Hytale.</td> 
  <td><b>SDK Resolution</b><br/> Automatically configure and detect JVMs and turn on DCEVM when possible.</td>
<td><b>True source linking</b><br/> DevServer & AssetEditor use your sources; no copy, instant feedback.</td>
</tr>
<tr>
  <td><b>Testing included</b><br/> JUnit or Kotest with coverage reporting out of the box.</td>
  <td><b>Pre-configured devserver</b><br/> Creative superflat with offline mode turned on for a quick start.</td>
<td>&nbsp;</td>
</tr>
</table>

<!-- <td><b>Source retrieval or decompiler</b><br/> Provide full IntelliSense without re-bundling sources for builds.</td> -->
<!-- <td><b>VSCode & Neovim support</b><br/> Auto-configure popular alternative development environments</td> -->
<!-- <td><b>CI/CI integration</b><br/> Generate popular pipelines for automation or have the hooks for custom solutions.</td> -->
<!-- <td><b>Publishing to CurseForge</b><br/> Remove one more obstacle to distribute your mod by autmating the process.</td> -->

> [!NOTE]
> Latest update (`0.2.7 → 0.2.9`):
> - Fixed missing `compileOnly` access in `dependencies` ([#2](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/2))
> - Fixed plugin not being available in the `runServer` task ([#8](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/8))
> - Disabled offline-mode as it is only meant for singleplayer ([#9](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/9))
> - Fixed missing arguments for `runServer` task ([#10](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/10))
> - Fixed `usePatchline` and `useVersion` not applying the desired configuration
> - Shelved offline-mode devserver as DevelopmentMode for the future when it's available
> - Fixed missed quotation around Asset and Mods paths, only when spaces detected

See the whole [CHANGELOG](CHANGELOG.md)

## Requirements

- IntelliJ IDEA (even Community edition), VS Code, or any other editor
- Java 25, use JetBrains Runtime (JBR) for hot swapping to work!
- Hytale installed through the launcher when used as the target

# Usage

```kotlin
// settings.gradle.kts
rootProject.name = "dev.example"
plugins {
    id("dev.scaffoldit") version "0.2.+"
}
hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.example.mod.ExamplePlugin"
        IncludesAssetPack = true
    }
}
```

This will expose several Gradle commands under the "hytale" group:

| Command                         | Description                                                                    |
|:--------------------------------|:-------------------------------------------------------------------------------|
| `gradlew setupServer`           | Create the the devserver, delete the folder yourself to make a complete reset! |
| `gradlew runServer`             | Runs the devserver, use -Ddebug for opening a debugger and allow hot-swapping. |
| `gradlew generateManifest`      | Generate the plugin manifest from settings and existing values.                |

# Configuration

Everything is supported in the `settings.gradle[.kts]`, but includes are not possible in
`build.gradle[.kts]` so the recommendation is to configure everything in one file:

## `common { }`

Collects and configures common library packages and adds them to the target projects as
dependencies. All `include()` projects are relative from the `common` directory. It automatically
creates the necessary folder structure but leaves the classes itself to you.

#### `projectDir: String`

Configures the parent directory for the `common` workspaces, by default, it is set to `common`,
reset it with an empty string to simply create all your projects in the repository root.

#### `repositories {}`, `dependencies {}`,  `include(...projectString)`, `include(projectString)`

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

### `useKotlin(dependencyString)`

Use and specify the Kotlin STDLib for compilation, left alone it will import the standard
`org.jetbrains.kotlin:kotlin-stdlib` but you can specify your own version, such as anything already
distributed on the curse maven.

This function sets the `kotlin: String?` property, so if you prefer, you can also simply assign your
stdlib using `kotlin = "my.kotlin.srdlib"`.

### `useFlat()`

Switch the directory layout to flat that drops `main/com/example/project`, this is useful for Kotlin
as it reduces the complexity of the project structure and the compiler still re-creates that within
your built jar file.

## `hytale { }`

Configures a hytale project with dependencies, manifest management, and inherits common packages
when they are declared. It automatically creates the necessary folder structure but leaves the
classes itself to you.

#### `projectDir: String`

Configures the parent directory for the `hytale` workspaces, by default, it is set to `hytale`,
reset it with an empty string to simply create all your projects in the repository root.

#### `usePatchline(String)`, `useVersion(String)`

Configure which patchline from maven to depend on and within that which version. By default, it will
use the RELEASE patchline with the "+" version, which means the latest.

#### `devserver {}`

- `Enabled: Boolean`: Controls if the devserver should be generated at all. (default=true)
- `AllowOp: Boolean`: Enables privileged server sessions for OP permissions. (default=true)
- `DisableSentry: Boolean`: Avoids spamming Hypixel with modding errors. (default=true)
- `AcceptEarlyPlugins: Boolean`: Sets your plugin to be loaded with the builtins (default=false)
- `IncludeUserMods: Boolean`: Links the launcher global mods into the devserver (default=false)

#### `repositories {}`, `dependencies {}`,  `include(...projectString)`, `include(projectString)`

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

### `useKotlin(dependencyString)`

Use and specify the Kotlin STDLib for compilation, left alone it will import the standard
`org.jetbrains.kotlin:kotlin-stdlib` but you can specify your own version, such as anything already
distributed on the curse maven.

This function sets the `kotlin: String?` property, so if you prefer, you can also simply assign your
stdlib using `kotlin = "my.kotlin.srdlib"`.

### `useFlat()`

Switch the directory layout to flat that drops `main/com/example/project`, this is useful for Kotlin
as it reduces the complexity of the project structure and the compiler still re-creates that within
your built jar file.

#### `manifest { }`

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

Where the `Author` accepts:

- `Name: String`: the display name shown in the mods list.
- `Website: String?`: Optional field, not shown anywhere for now.
- `Email: String?`: Optional field, not shown anywhere for now.

## Hot Swapping

To enable dynamic class reloading at runtime, you MUST use a JetBrains JRE, which is available for
free at https://github.com/JetBrains/JetBrainsRuntime, or you can manage it within the IDEA SDK
window. Once configured, update your run configuraitons to use it and watch for the Gradle widget
in your code section's top right corner.

Scaffoldit also ships Agent Plugin that at runtime detects the Hot Swapping and reloads plugins
automatically for you; with that you only need to click on "Code Changed" in the editor UI. If you
only need this agent, you can use it by adding:

```kotlin
dependencies {
    runtimeOnly("dev.scaffoldit:devtools:0.2.+")
}
```

> [!NOTE]
> You only need this if you do not use the ScaffoldIt Plugin itself!

## Under the hood toggles

While the plugin sets up a complete development environment, you can fine-tune that by setting
the included package versions, and their feature flags in the `gradle.properties`:

```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
# Use these after you stop touching your settings:
org.gradle.configuration-cache=true
org.gradle.caching=true
# Under the hood settings:
env.java.version=25
env.java.compileSources=true
env.java.compileDocs=false
env.scaffoldit.kotlinSupport=true
env.scaffoldit.kotlinTest=true
env.scaffoldit.kotlinTest.version=6.1.1
env.scaffoldit.javaUnitTest=true
env.scaffoldit.javaUnitTest.version=5.10.1
env.scaffoldit.jacocoCoverage=true
env.scaffoldit.autoToolchain=true
env.scaffoldit.monorepoSupport=true
env.scaffoldit.monorepoAutoDepends=true
env.scaffoldit.globalAssets=true
env.hytale.devServerDCEVM=true
env.hytale.devServerHotReload=true
env.hytale.manifestGenerator=true
env.hytale.configureIdea=true
# Example: D:\\HYTALE\\Data
hytale.home_path=""
```

# Contributions

Feel free to open issues or pull requests if you have some problems, you can also reach out to me
on discord under the nickname of `kicsivazz`.
