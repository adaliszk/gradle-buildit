rootProject.name = "dev.scaffoldit"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

include("api", "gradle", "devtools")
include("common", "hytale")
