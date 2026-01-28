rootProject.name = "dev.scaffoldit.examples.java"

plugins {
    id("dev.scaffoldit") version "0.2.2"
}

hytale {
    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.JavaExample"
    }
}