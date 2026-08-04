package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.annotation.LoaderPriority
import net.miarma.mkernel.common.model.LastPosition
import net.miarma.mkernel.common.service.IService
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
@LoaderPriority(LoaderPriority.LOW)
class LastPositionService @Inject constructor() : IService {

    private val positions = ConcurrentHashMap<UUID, LastPosition>()

    fun setLastPosition(uuid: UUID?, location: Location?) {
        uuid ?: return
        positions.computeIfAbsent(uuid) { LastPosition() }.set(location)
    }

    fun setLastPosition(player: Player?, location: Location?) {
        player ?: return
        setLastPosition(player.uniqueId, location)
    }

    fun getLastPosition(uuid: UUID?): Location? {
        uuid ?: return null
        return positions[uuid]?.get()
    }

    fun getLastPosition(player: Player?): Location? {
        player ?: return null
        return getLastPosition(player.uniqueId)
    }

    fun remove(uuid: UUID?) {
        uuid ?: return
        positions.remove(uuid)
    }

    fun remove(player: Player?) {
        player ?: return
        remove(player.uniqueId)
    }

    fun hasLastPosition(uuid: UUID): Boolean {
        return positions[uuid]?.hasPosition() ?: false
    }
}
