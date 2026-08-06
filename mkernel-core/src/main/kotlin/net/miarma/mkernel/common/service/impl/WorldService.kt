package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.common.dao.WorldDao
import org.bukkit.Bukkit

@Singleton
@LoaderPriority(LoaderPriority.HIGH)
class WorldService @Inject constructor(
    private val plugin: MKernel,
    private val worldDao: WorldDao
) : IService {

    override fun onEnable() {
        plugin.launchAsync {
            Bukkit.getWorlds().forEach { worldDao.createWorld(it) }
        }
    }
}