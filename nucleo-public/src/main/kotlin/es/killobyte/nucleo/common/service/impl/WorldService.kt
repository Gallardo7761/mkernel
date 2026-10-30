package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.common.dao.WorldDao
import org.bukkit.Bukkit

@Singleton
@LoaderPriority(LoaderPriority.HIGH)
class WorldService @Inject constructor(
    private val plugin: NucleoPlugin,
    private val worldDao: WorldDao
) : IService {

    override fun onEnable() {
        plugin.launchAsync {
            Bukkit.getWorlds().forEach { worldDao.createWorld(it) }
            worldDao.loadBlockedWorldsCache()
        }
    }
}