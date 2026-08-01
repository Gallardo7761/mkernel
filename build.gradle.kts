import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "net.miarma"
version = "26.8.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.pcgamingfreaks.at/repository/maven-everything")
    maven("https://jitpack.io")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://git.miarma.net/api/packages/Gallardo7761/maven")
    maven("https://raw.githubusercontent.com/JorelAli/1.13-Command-API/mvn-repo/1.13CommandAPI/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:[26.1.2.build.1, 26.1.2.build.9999)")
    compileOnly("at.pcgamingfreaks:Minepacks-API:2.5.5")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.3-SNAPSHOT")
    compileOnly("com.github.GriefPrevention:GriefPrevention:18.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.placeholderapi:placeholderapi:2.11.6")
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:5.2.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.xerial:sqlite-jdbc:3.53.2.0")

    implementation("net.miarma:mscript:26.7.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.javassist:javassist:3.28.0-GA")
    implementation("dev.jorel:commandapi-paper-shade:11.2.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.2.0")
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
    implementation("com.google.inject:guice:7.0.0:classes")
    implementation("org.ow2.asm:asm:9.9.1")
    implementation("xyz.xenondevs.invui:invui:2.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(25)
    }

    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("25"))
        }
    }

    named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveFileName.set("MKernel-${project.version}.jar")
        destinationDirectory.set(file("/home/jomaa/Escritorio/server/plugins"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
    }

    build {
        dependsOn("shadowJar")
    }
}