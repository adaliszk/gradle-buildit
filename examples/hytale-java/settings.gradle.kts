rootProject.name = "dev.scaffoldit.examples.java"

plugins {
    id("dev.scaffoldit") version "0.1.15"
}

hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.JavaExample"
    }
}