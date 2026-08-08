import java.io.File
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.yaml:snakeyaml:2.6")
    }
}

plugins {
    id("com.gradleup.shadow")
}

fun getGitHash(): String {
    return runCatching {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(rootDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    }.getOrDefault("nogit")
}

val buildVersion = "${project.version}+${getGitHash()}"

dependencies {
    implementation(project(":mkernel-api"))
    implementation(project(":mkernel-core"))

    compileOnly("com.google.inject:guice:7.0.0:classes")
    compileOnly("xyz.xenondevs.invui:invui:2.1.1")
    compileOnly("io.papermc.paper:paper-api:[26.1.2.build.1, 26.1.2.build.9999)")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")
    compileOnly("ovh.mythmc:banco-api:1.2.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17")
    compileOnly("dev.jorel:commandapi-paper-shade:11.2.0")
    compileOnly("dev.jorel:commandapi-kotlin-paper:11.2.0")
    compileOnly("io.github.arcaneplugins:levelledmobs-plugin:4.0.3.1")
}

tasks.register("mergeScriptsYaml") {
    dependsOn(project(":mkernel-core").tasks.named("processResources"))
    dependsOn("processResources")

    doLast {
        val yaml = Yaml(DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
            indent = 2
            indicatorIndent = 0
        })

        val coreResDir = project(":mkernel-core").layout.buildDirectory.dir("resources/main").get().asFile
        val localResSrcDir = file("src/main/resources")
        val outDir = layout.buildDirectory.dir("resources/main").get().asFile

        val coreFile = File(coreResDir, "scripts.yml")
        val localFile = File(localResSrcDir, "scripts.yml")
        val outFile = File(outDir, "scripts.yml")

        if (coreFile.exists() && localFile.exists()) {
            println("MiarmaCraft -> SnakeYAML Merge: scripts.yml")

            val coreData: MutableMap<String, Any> = yaml.load(coreFile.readText(Charsets.UTF_8)) ?: mutableMapOf()
            val localData: Map<String, Any>? = yaml.load(localFile.readText(Charsets.UTF_8))

            if (localData != null) {
                @Suppress("UNCHECKED_CAST")
                val coreScripts = coreData.getOrPut("scripts") { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                @Suppress("UNCHECKED_CAST")
                val localScripts = localData["scripts"] as? Map<String, Any>

                if (localScripts != null) {
                    coreScripts.putAll(localScripts)
                }
            }

            outFile.parentFile.mkdirs()
            outFile.writeText(yaml.dump(coreData), Charsets.UTF_8)
            println("MiarmaCraft -> scripts.yml merged successfully!")

        } else if (coreFile.exists()) {
            coreFile.copyTo(outFile, overwrite = true)
        } else if (localFile.exists()) {
            localFile.copyTo(outFile, overwrite = true)
        }
    }
}

tasks.register("mergeInitSql") {
    dependsOn(project(":mkernel-core").tasks.named("processResources"))
    dependsOn("processResources")

    doLast {
        val coreResDir = project(":mkernel-core").layout.buildDirectory.dir("resources/main").get().asFile
        val localResDir = layout.buildDirectory.dir("resources/main").get().asFile

        val coreSql = File(coreResDir, "init.sql")
        val localSqlSrc = file("src/main/resources/init.sql")
        val outputFile = File(localResDir, "init.sql")

        val merged = StringBuilder()
        if (coreSql.exists()) {
            println("MiarmaCraft -> Merge SQL: core init.sql")
            merged.append(coreSql.readText(Charsets.UTF_8).trimEnd()).append("\n\n")
        }
        if (localSqlSrc.exists()) {
            println("MiarmaCraft -> Merge SQL: miarmacraft init.sql")
            merged.append(localSqlSrc.readText(Charsets.UTF_8).trimEnd()).append("\n")
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(merged.toString(), Charsets.UTF_8)
    }
}

tasks {
    processResources {
        val props = mapOf("version" to buildVersion)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching(listOf("paper-plugin.yml")) {
            expand(props)
        }
    }

    named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        dependsOn("mergeScriptsYaml")
        dependsOn("mergeInitSql")
        archiveFileName.set("mkernel-$buildVersion.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }
}