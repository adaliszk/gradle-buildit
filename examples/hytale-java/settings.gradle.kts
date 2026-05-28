rootProject.name = "dev.scaffoldit.example.java"

pluginManagement {
    includeBuild("../..")
}

plugins {
    id("dev.scaffoldit") // version "0.2.3"
}

hytale {
    useVersion("0.5.2")

    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.java.JavaExample"
        ServerVersion = ">=0.5.2"
    }
}