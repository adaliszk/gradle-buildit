rootProject.name = "dev.scaffoldit.example.kotlin"

pluginManagement {
    includeBuild("../..")
}

plugins {
    id("dev.scaffoldit") // version "0.2.3"
}

hytale {
    useKotlin("curse.maven:kytale-1428431:7464573")
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.kotlin.KotlinExample"
    }
}