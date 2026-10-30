plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":nucleo-api"))

    compileOnly("io.papermc.paper:paper-api:[26.3.build.1, 26.3.build.9999)")
    compileOnly("at.pcgamingfreaks:Minepacks-API:2.5.9.1")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.16.1")
    compileOnly("com.github.GriefPrevention:GriefPrevention:18.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.placeholderapi:placeholderapi:2.11.6")
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:5.2.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.xerial:sqlite-jdbc:3.53.2.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.10.1")
    compileOnly("ovh.mythmc:banco-api:1.2.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.19") {
        // WorldGuard pins Guava/Gson strictly; Paper provides them at runtime
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.code.gson", module = "gson")
    }

    implementation("es.killobyte:nscript:26.10.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.javassist:javassist:3.33.0-GA")
    implementation("dev.jorel:commandapi-paper-shade:12.1.0") {
        isTransitive = false
    }
    implementation("dev.jorel:commandapi-kotlin-paper:12.1.0") {
        exclude(group = "dev.jorel", module = "commandapi-paper-shade")
    }
    implementation("dev.dejvokep:boosted-yaml:1.3.7")
    implementation("com.google.inject:guice:7.0.0:classes")
    implementation("org.ow2.asm:asm:9.9.1")
    implementation("xyz.xenondevs.invui:invui:2.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0")
}

tasks {
    named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveFileName.set("nucleo-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching(listOf("paper-plugin.yml", "config.yml")) {
            expand(props)
        }
    }

    build {
        dependsOn("shadowJar")
    }
}