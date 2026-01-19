rootProject.name = "dev.buildit"

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.3.1"
        kotlin("jvm") version "2.3.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    includeBuild("gradle")
}

//
// ----------------------------------------------------------------------------
// From this point on dogfooding, as in use the plugin like normal:
//

plugins {
    id("dev.buildit.repository")
}

buildit {
    useKotlin()
}

common {
    // Main libraries that the rest of them builds on
    include("foundation", "configs", "commands")
    include("processors", "schematics", "ponder")

    // Foundation components that the content relies on
    include("kinetics", "logistics", "contraptions", "railways")
    include("equipments", "decorations")
}

hytale {
    includeAssetPack = true
}
