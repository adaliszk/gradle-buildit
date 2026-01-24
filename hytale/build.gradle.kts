plugins {
    id("com.github.adaliszk.gradle-scaffoldit-modkit")
}

project.group = "dev.scaffoldit.hytale"
project.version = System.getenv("GITHUB_REF_NAME") ?: "0.0.0-dev"

hytale {
    useKotlin()
    useFlat()
    manifest {
        Group = "ScaffoldIt"
        Name = "Devtools"
        Main = "dev.scaffoldit.hytale.DevelopmentPlugin"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy-agent:1.14.18")
}

//repositories {
//    mavenCentral()
//    maven("https://repo.hotswapagent.org/")
//}
//
//dependencies {
//    implementation("org.hotswapagent:hotswap-agent-core:2.0.3")
//}