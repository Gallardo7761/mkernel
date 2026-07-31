package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.IService
import net.miarma.mkernel.common.teleport.TpaRequest
import net.miarma.mkernel.common.teleport.TpaType
import org.bukkit.entity.Player
import java.util.function.Consumer

@Singleton
class TeleportService @Inject constructor(private val databaseService: DatabaseService) : IService {

    fun addRequest(from: Player, to: Player, type: TpaType) {
        databaseService.insertTeleportRequest(
            from.uniqueId.toString(),
            to.uniqueId.toString(),
            type == TpaType.TPA
        )
    }

    fun getTpaRequest(from: Player, to: Player, callback: Consumer<TpaRequest?>) {
        databaseService.getTpaRequest(from, to, callback)
    }

    fun getIncomingTpaRequest(to: Player, callback: Consumer<TpaRequest?>) {
        databaseService.getIncomingTpaRequest(to, callback)
    }

    fun removeRequest(request: TpaRequest) {
        databaseService.deleteTeleportRequest(
            request.from.uniqueId.toString(),
            request.to.uniqueId.toString()
        )
    }
}
