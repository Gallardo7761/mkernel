package net.miarma.mkernel.util

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object PlayerUtil {

    fun isWithinRadius(e1: Entity, e2: Entity, radius: Int): Boolean {
        if (e1.world != e2.world) return false
        return e1.location.distanceSquared(e2.location) <= (radius * radius)
    }

    fun playersNearRadius(player: Player, players: Collection<Player>, radius: Int): Boolean {
        return players.any { p -> p != player && isWithinRadius(p, player, radius) }
    }

    fun <E : Entity> isEntityNear(player: Player, entityClass: Class<E>, radius: Int): Boolean {
        return player.location.chunk.entities.any { entity ->
            entityClass.isInstance(entity) && isWithinRadius(player, entity, radius)
        }
    }

    fun fromArg(argumentObject: Any?): Player? {
        return when (argumentObject) {
            is Collection<*> -> {
                val first = argumentObject.firstOrNull()
                (first as? PlayerProfile)?.id?.let { Bukkit.getPlayer(it) }
            }
            else -> null
        }
    }
}
