> [!WARNING]  
> In active development and experimentation, almost nothing is stable!

# BuildIt! Gradle Plugin for Mod Development

The philosophy here is to keep the necessary boilerplate at the absolute minimum while also
providing a robust and flexible framework for mod development for Hytale, _later to Minecraft._

### Features

- Automatic server dependency resolution and manifest generation
- Minimal boilerplate where you have access to `hytale` block in both settings and build scripts
- Monorepo support through the usage via `include` within the `common` configuration block
- Kotlin support by using `useKotlin()` in the `buildit` configuration block

_and more coming soon:tm:_

### Requirements

- IntelliJ IDEA (even Community edition)
- Java 25, preferably JetBrains downloaded and set as default
- Hytale installed through the launcher

## Usage

```kotlin
// settings.gradle.kts
rootProject.name = "dev.example"
pluginManagement {
    plugins {
        id("com.github.adaliszk.gradle-buildit") version "dev-snapshot"
        id("com.gradleup.shadow") version "9.3.1"
    }
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
plugins {
    id("com.github.adaliszk.gradle-buildit")
}
hytale {
    includeAssetPack()
}
```

### Hytale {}

Resolves either the root or the hytale folder for your mod development, where it automatically
detects which one by seeing if you used the `common { include() }` before. It automatically creates
the necessary folder structure and even creates an example main class for you.

Options:

- `includeAssetPack(Boolean)`: Whether to include the asset pack via the manifest (default: true)

### Common {}

Collects and configures common library packages and adds them to the target projects as
dependencies, similarly to `hytale` it automatically creates the necessary folder structure, but
will leave the main class up to you.

Options:

- `include(...Strings) { }`: Include the specified package directory in the common configuration.
- `useKotlin(dependencyNotation)`: Enable kotlin support only for the common projects.

### BuildIt {}

Defaults for all the configuration blocks and templates, mainly to support multiple

Options:

- `useKotlin(dependencyNotation)`: Enable kotlin support only for the buildit projects.
- `useFlat()`: Turn on flat project structure where `main/java/dev/example` is dropped.
- `sourceDir = "String"`: The source directory relative from the project path.
- `resourceDir = "String"`: The resource directory relative from the project path.
- `testDir = "String"`: The test directory relative from the project path.
- `commonDir = "String"`: Global common path relative from the root project.
- `assetDir = "String"`: Global asset directory relative from the root project.

## Contributions

Feel free to open issues or pull requests if you have some problems, you can also reach out to me
on discord under the nickname of `kicsivazz`.

Current plan:

- [ ] IDEA run configuration generator
- [ ] Java and Kotlin boilerplate for quick project creation
- [ ] Bootstrap devserver with a superflat testing world
- [ ] Examples for Kotlin, Monorepo Java, Monorepo Kotlin, Monorepo Mixed
- [ ] Bootstrap tests with a boilerplate
- [ ] Proof of Concept for Kotlin Annotation-based wiring
- [ ] Auto hot-swapping instead of manual IDEA button usage
