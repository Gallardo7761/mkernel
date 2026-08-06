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

dependencies {
    implementation(project(":mkernel-api"))
    implementation(project(":mkernel-core"))

    compileOnly("com.google.inject:guice:7.0.0:classes")
    compileOnly("xyz.xenondevs.invui:invui:2.1.1")
    compileOnly("io.papermc.paper:paper-api:[26.1.2.build.1, 26.1.2.build.9999)")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")
}

tasks.register("deepMergeYamls") {
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
        val localResDir = layout.buildDirectory.dir("resources/main").get().asFile
        val filesToMerge = listOf("config.yml", "commands.yml", "messages.yml", "paper-plugin.yml", "scripts.yml")

        fun mergeMaps(target: MutableMap<String, Any>, source: Map<String, Any>) {
            for ((key, value) in source) {
                if (value == null) continue

                if (value is Map<*, *> && target[key] is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    mergeMaps(target[key] as MutableMap<String, Any>, value as Map<String, Any>)
                } else if (value is List<*> && target[key] is MutableList<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val targetList = target[key] as MutableList<Any>
                    for (item in value as List<Any>) {
                        if (item != null && !targetList.contains(item)) {
                            targetList.add(item)
                        }
                    }
                } else {
                    target[key] = value
                }
            }
        }

        for (fileName in filesToMerge) {
            val coreFile = File(coreResDir, fileName)
            val localFile = File(localResDir, fileName)

            if (coreFile.exists()) {
                val coreData: MutableMap<String, Any> = yaml.load(coreFile.readText()) ?: mutableMapOf()

                if (localFile.exists()) {
                    println("MiarmaCraft -> Deep Merge: $fileName")
                    val localData: Map<String, Any>? = yaml.load(localFile.readText())
                    if (localData != null) {
                        mergeMaps(coreData, localData)
                    }
                }

                localFile.writeText(yaml.dump(coreData))
            }
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
            merged.append(coreSql.readText().trimEnd()).append("\n\n")
        }
        if (localSqlSrc.exists()) {
            println("MiarmaCraft -> Merge SQL: miarmacraft init.sql")
            merged.append(localSqlSrc.readText().trimEnd()).append("\n")
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(merged.toString())
    }
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching(listOf("paper-plugin.yml", "config.yml")) {
            expand(props)
        }
    }

    named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        dependsOn("deepMergeYamls")
        dependsOn("mergeInitSql")
        archiveFileName.set("mkernel-miarmacraft-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }
}