package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.dejvokep.boostedyaml.YamlDocument
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mscript.MScriptEngine
import java.io.File
import java.io.IOException

@Singleton
@LoaderPriority(LoaderPriority.NORMAL)
class ScriptService @Inject constructor(private val plugin: MKernel) : IService {

    var scriptEngine: MScriptEngine? = null
        private set

    override fun onEnable() {
        try {
            val configFile = File(plugin.dataFolder, "scripts.yml")
            if (!configFile.exists()) {
                plugin.saveResource("scripts.yml", false)
            }

            val config = YamlDocument.create(configFile)
            scriptEngine = MScriptEngine(plugin, config).also { it.onEnable() }
        } catch (e: IOException) {
            plugin.logger.severe("Error initializing mscript engine: ${e.message}")
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
