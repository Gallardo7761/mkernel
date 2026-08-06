package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.block.implementation.Section
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IConfigService
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.api.model.World
import net.miarma.mkernel.common.config.ConfigKeys
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoaderPriority(LoaderPriority.HIGHEST)
class ConfigService @Inject constructor(private val plugin: MKernel) : IService, IConfigService {

    private val configs = mutableMapOf<String, YamlDocument>()
    private val cache = ConcurrentHashMap<String, Any?>()

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
                plugin.logger.severe("Error finding internal resource: $fileName")
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
            plugin.logger.info("Configuration loaded/updated: $fileName")
        } catch (e: IOException) {
            plugin.logger.severe("Error reloading file: $fileName")
            e.printStackTrace()
        }
    }

    fun reloadAll() {
        configs.values.forEach { config ->
            try {
                config.reload()
            } catch (e: IOException) {
                plugin.logger.severe("Error reloading file: ${config.file?.name}")
                e.printStackTrace()
            }
        }
        cache.clear()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getString(path: String, def: String): String {
        return cache.getOrPut("str:$path") {
            configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getString(path) else null } ?: def
        } as String
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringList(path: String): List<String> {
        return cache.getOrPut("list:$path") {
            configs.values.firstNotNullOfOrNull {
                if (it.contains(path)) it.getStringList(path) else null
            } ?: emptyList<String>()
        } as List<String>
    }

    override fun getInt(path: String, def: Int): Int {
        return cache.getOrPut("int:$path") {
            configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getInt(path) else null } ?: def
        } as Int
    }

    override fun getBoolean(path: String, def: Boolean): Boolean {
        return cache.getOrPut("bool:$path") {
            configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getBoolean(path) else null } ?: def
        } as Boolean
    }

    fun getFloat(path: String, def: Float = 0.0f): Float {
        return cache.getOrPut("float:$path") {
            configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getFloat(path) else null } ?: def
        } as Float
    }

    override fun getDouble(path: String, def: Double): Double {
        return cache.getOrPut("double:$path") {
            configs.values.firstNotNullOfOrNull { if (it.contains(path)) it.getDouble(path) else null } ?: def
        } as Double
    }

    fun getSection(path: String, def: Section? = null): Section? {
        return configs.values.firstNotNullOfOrNull {
            if (it.contains(path)) it.getSection(path) else null
        } ?: def
    }

    fun set(path: String, value: Any?) {
        val targetConfig = configs.values.firstOrNull { it.contains(path) } ?: configs["config.yml"]
        targetConfig?.set(path, value)
        targetConfig?.save()
        cache.keys.removeIf { it.endsWith(":$path") }
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        if (configs.values.any { it.contains(moduleName) && it.isBoolean(moduleName) }) {
            return getBoolean(moduleName, true)
        }
        val enabledPath = "$moduleName.enabled"
        return getBoolean(enabledPath, true)
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