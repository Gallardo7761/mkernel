package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.common.dao.TeleportDao
import es.killobyte.nucleo.common.teleport.TpaRequest
import es.killobyte.nucleo.common.teleport.TpaType
import org.bukkit.entity.Player

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class TeleportService @Inject constructor(
    private val plugin: NucleoPlugin,
    private val teleportDao: TeleportDao
) : IService {

    fun addRequest(from: Player, to: Player, type: TpaType) {
        plugin.launchAsync {
            teleportDao.insertTeleportRequest(
                from.uniqueId.toString(),
                to.uniqueId.toString(),
                type == TpaType.TPA
            )
        }
    }

    suspend fun getTpaRequest(from: Player, to: Player): TpaRequest? {
        return teleportDao.getTpaRequest(from, to)
    }

    suspend fun getIncomingTpaRequest(to: Player): TpaRequest? {
        return teleportDao.getIncomingTpaRequest(to)
    }

    fun removeRequest(request: TpaRequest) {
        plugin.launchAsync {
            teleportDao.deleteTeleportRequest(
                request.from.uniqueId.toString(),
                request.to.uniqueId.toString()
            )
        }
    }
}