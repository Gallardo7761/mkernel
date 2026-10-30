package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.dejvokep.boostedyaml.YamlDocument
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nscript.NScriptEngine
import java.io.File
import java.io.IOException

@Singleton
@LoaderPriority(LoaderPriority.NORMAL)
class ScriptService @Inject constructor(private val plugin: NucleoPlugin) : IService {

    var scriptEngine: NScriptEngine? = null
        private set

    override fun onEnable() {
        try {
            val configFile = File(plugin.dataFolder, "scripts.yml")
            if (!configFile.exists()) {
                plugin.saveResource("scripts.yml", false)
            }

            val config = YamlDocument.create(configFile)
            scriptEngine = NScriptEngine(plugin, config).also { it.onEnable() }
        } catch (e: IOException) {
            plugin.logger.severe("Error initializing nscript engine: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        scriptEngine?.onDisable()
    }

    fun reloadScripts() {
        scriptEngine?.let {
            try {
                it.scriptParser.config.reload()
                it.loadScripts()
            } catch (e: IOException) {
                plugin.logger.severe("Error reloading scripts.yml: ${e.message}")
            }
        }
    }
}
