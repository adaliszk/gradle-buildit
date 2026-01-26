rootProject.name = "dev.scaffoldit.examples.kotlin"

plugins {
    id("dev.scaffoldit") version "0.1.15"
}

hytale {
    useKotlin()
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.KotlinExample"
    }
}