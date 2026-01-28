rootProject.name = "dev.scaffoldit.examples.kotlin"

plugins {
    id("dev.scaffoldit") version "0.2.2"
}

hytale {
    useKotlin()
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.KotlinExample"
    }
}