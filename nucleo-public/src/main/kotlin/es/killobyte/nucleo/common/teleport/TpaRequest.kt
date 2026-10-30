package es.killobyte.nucleo.common.teleport

import org.bukkit.entity.Player

data class TpaRequest(
    val from: Player,
    val to: Player,
    val type: TpaType,
    val timestamp: Long = System.currentTimeMillis()
)
