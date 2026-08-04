package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.miarma.mkernel.common.annotation.LoaderPriority
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.model.World
import net.miarma.mkernel.common.service.IService
import java.io.File
import java.io.IOException

@Singleton
@LoaderPriority(LoaderPriority.HIGHEST)
class ConfigService @Inject constructor(private val plugin: MKernel) : IService {

    private val configs = mutableMapOf<String, YamlDocument>()

    override fun onEnable() {
        loadConfigFile("config.yml")
        loadConfigFile("commands.yml")
        loadConfigFile("messages.yml")
    }

    fun getConfig(name: String): YamlDocument? {
        return configs[name]
    }

    private fun loadConfigFile(fileName: String) {
        try {
            val file = File(plugin.dataFolder, fileName)
            val resource = plugin.getResource(fileName) ?: run {
                MKernel.LOGGER.severe("Error finding internal resource: $fileName")
                return
            }

            val document = YamlDocument.create(
                file,
                resource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                    .setVersioning(BasicVersioning("file-version"))
                    .setKeepAll(true)
                    .build()
            )
            configs[fileName] = document
            MKernel.LOGGER.info("Configuration loaded/updated: $fileName")
        } catch (e: IOException) {
            MKernel.LOGGER.severe("Error reloading file: $fileName")
            e.printStackTrace()
        }
    }

    fun reloadAll() {
        configs.values.forEach { config ->
            try {
                config.reload()
            } catch (e: IOException) {
                MKernel.LOGGER.severe("Error reloading file: ${config.file?.name}")
                e.printStackTrace()
            }
        }
    }

    fun getString(path: String, def: String = ""): String {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getString(path) else null } ?: def
    }

    fun getStringList(path: String): List<String> {
        return configs.values.firstNotNullOfOrNull {
            if (it.contains(path)) it.getStringList(path) else null
        } ?: emptyList()
    }

    fun getInt(path: String, def: Int = 0): Int {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getInt(path) else null } ?: def
    }

    fun getBoolean(path: String, def: Boolean = false): Boolean {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getBoolean(path) else null } ?: def
    }

    fun getFloat(path: String, def: Float = 0.0f): Float {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getFloat(path) else null } ?: def
    }

    fun getDouble(path: String, def: Double = 0.0): Double {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getDouble(path) else null } ?: def
    }

    fun getSection(path: String, def: Section? = null): Section? {
        return configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getSection(path) else null } ?: def
    }

    fun set(path: String, value: Any?) {
        val targetConfig = configs.values.firstOrNull { it.contains(path) } ?: configs["config.yml"]
        targetConfig?.set(path, value)
        targetConfig?.save()
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        return getBoolean(moduleName, true)
    }

    fun getLobbyWorld(): World {
        return World(
            name = getString(ConfigKeys.Settings.Worlds.LOBBY_NAME, "lobby"),
            x = getDouble(ConfigKeys.Settings.Worlds.LOBBY_X, 0.5),
            y = getDouble(ConfigKeys.Settings.Worlds.LOBBY_Y, 65.0),
            z = getDouble(ConfigKeys.Settings.Worlds.LOBBY_Z, 0.5),
            yaw = getInt(ConfigKeys.Settings.Worlds.LOBBY_YAW, 180),
            pitch = getInt(ConfigKeys.Settings.Worlds.LOBBY_PITCH, 0)
        )
    }
}