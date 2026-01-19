> [!WARNING]  
> In active development and experimentation, almost nothing is stable!

_Doing this because I want to learn and eventually write my own mods, so expect rookie mistakes when
it comes to Java/Kotlin/Gradle practices!_

# BuildIt! Gradle Plugin for Mod Development

The philosophy here is to keep the necessary boilerplate at the absolute minimum while also
providing a robust and flexible framework for mod development for Hytale, then later to Minecraft.

```kotlin
// settings.gradle.kts

rootProject.name = "dev.example"

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
        id("dev.buildit.repository") version "dev-snapshot"
        // ^ Settings plugin for the sections below
    }
    repositories {
        maven("https://jitpack.io")
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("dev.buildit.repository")
}

buildit {
    useKotlin() // <- Optionally enable Kotlin, pass dependency string for your own stdlib!
}

// Include projects from "libraries"
common {
    include("api", "another")
    // ^ Auto-scaffolds these
}

// Include "targets/hytale" automatically
hytale {
    includeAssetPack = true
}
```

Developed for a monorepo structure (though it can be also used for regular structure). You have
access to common libraries that hold your data and functionality between them so that your actual
mod would only need to wire up components and events.

This is expanded with:

## ScaffoldIt! Framework for cross-platform Mods

> [!NOTE]  
> Entirely a concept so far, nothing is implemented!

Taking an idea from Stonecutter for extending the compiler, the main concept with the Scaffolding is
to provide annotations that will inject commonly used wiring without you needing to know exactly how
each game or mod-loader provides those functionalities.

The overall idea is that your mod code will mainly be about wiring known behaviors, interfaces, and
components without you needing to rewrite the entire mod for each scenario:

```kotlin
@Definition
class BuilditPlugin(init: JavaPluginInit) : JavaPlugin(init) {
    init {
        use(MyAddonLoader)
        use(BackgroundTasks)
    }

    @Hook(Lifecycle.LOAD)
    fun onSetup() {
        // Custom code here
    }
}

@Block
class CreativeMotor : Component<ChunkStore> {
    init {
        use(ConfigurableSpeed)
        use(KineticProducer.of {
            abs(RPM) * 4096
        })
    }
}

@Block
class MechanicalPress : Component<ChunkStore> {
    init {
        use(KineticRelay)
        use(KineticConsumer.of(rpm = 8.0f))
        use(RecipeProcessor.perCraft {
            tick = 240 / (1 + (abs(RPM) / 512f) * 59)
        })
    }
}
```

The rest on how those register is fully automated, and the various components used have
a platform-independent format, and the ScaffoldIt annotations deal with the wiring.
