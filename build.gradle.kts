import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.4.10" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
}

allprojects {
    group = "net.miarma"
    version = "26.8.7"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.pcgamingfreaks.at/repository/maven-everything")
        maven("https://jitpack.io")
        maven("https://repo.xenondevs.xyz/releases")
        maven("https://git.miarma.net/api/packages/Gallardo7761/maven")
        maven("https://raw.githubusercontent.com/JorelAli/1.13-Command-API/mvn-repo/1.13CommandAPI/")
        maven("https://repo.mythmc.ovh/releases")
        maven("https://maven.enginehub.org/repo/")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("25"))
        }
    }
}