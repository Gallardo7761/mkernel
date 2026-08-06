package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.annotation.LoaderPriority
import net.miarma.mkernel.api.common.IService
import net.miarma.mkernel.common.dao.TeleportDao
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.common.teleport.TpaType
import org.bukkit.entity.Player

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class TeleportService @Inject constructor(
    private val plugin: MKernel,
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