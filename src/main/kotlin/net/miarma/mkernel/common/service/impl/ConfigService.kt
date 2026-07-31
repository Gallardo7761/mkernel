package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.miarma.mkernel.common.model.World
import net.miarma.mkernel.common.service.IService
import java.io.File
import java.io.IOException

@Singleton
class ConfigService @Inject constructor(private val plugin: MKernel) : IService {

    private val configs = mutableMapOf<String, YamlDocument>()

    override fun onEnable() {
        loadConfigFile("config.yml")
    }

    fun getConfig(name: String): YamlDocument? {
        return configs[name]
    }

    private fun loadConfigFile(fileName: String) {
        try {
            val file = File(plugin.dataFolder, fileName)
            val resource = plugin.getResource(fileName) ?: run {
                MKernel.LOGGER.severe("Could not find the resource: $fileName")
                return
            }

            val document = YamlDocument.create(
                file,
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                dev.dejvokep.boostedyaml.settings.dumper.DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                    .setVersioning(BasicVersioning("file-version"))
                    .setKeepAll(true)
                    .build()
            )
            configs[fileName] = document
        } catch (e: IOException) {
            MKernel.LOGGER.severe("Failed to load configuration file: $fileName")
            e.printStackTrace()
        }
    }

    fun reloadAll() {
        configs.values.forEach { config ->
            try {
                config.reload()
            } catch (e: IOException) {
                MKernel.LOGGER.severe("Failed to reload a config file: ${config.file?.name}")
                e.printStackTrace()
            }
        }
    }

    fun getString(path: String): String {
        return configs["config.yml"]?.getString(path, "") ?: ""
    }

    fun getStringList(path: String): List<String> {
        return configs["config.yml"]?.getStringList(path) ?: emptyList()
    }

    fun getInt(path: String): Int {
        return configs["config.yml"]?.getInt(path) ?: 0
    }

    fun getBoolean(path: String): Boolean {
        return configs["config.yml"]?.getBoolean(path) ?: false
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        return getBoolean("config.modules.$moduleName")
    }

    fun getLobbyWorld(): World {
        val config = configs["config.yml"]!!
        return World(
            name = config.getString("config.worlds.lobby.name"),
            x = config.getDouble("config.worlds.lobby.coords.x"),
            y = config.getDouble("config.worlds.lobby.coords.y"),
            z = config.getDouble("config.worlds.lobby.coords.z"),
            yaw = config.getInt("config.worlds.lobby.coords.yaw"),
            pitch = config.getInt("config.worlds.lobby.coords.pitch")
        )
    }
}
