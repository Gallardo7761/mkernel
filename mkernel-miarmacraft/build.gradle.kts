import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import java.io.File

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

    compileOnly("io.papermc.paper:paper-api:[26.1.2.build.1, 26.1.2.build.9999)")
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

tasks {
    named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        dependsOn("deepMergeYamls")
        archiveFileName.set("mkernel-miarmacraft-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }
}