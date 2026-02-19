rootProject.name = "dev.scaffoldit.example.java"

pluginManagement {
    includeBuild("../..")
}

plugins {
    id("dev.scaffoldit") // version "0.2.3"
}

hytale {
    // usePatchline("pre-release")

    repositories {
        maven("https://repo.codemc.io/repository/creatorfromhell")
    }

    dependencies {
        compileOnly("net.cfh.vault:VaultUnlocked:2.18.3")
    }

    manifest {
        Group = "ScaffoldIt"
        Name = "Example"
        Main = "dev.scaffoldit.example.java.JavaExample"
        ServerVersion = "2026.02.18-f3b8fff95"
    }
}