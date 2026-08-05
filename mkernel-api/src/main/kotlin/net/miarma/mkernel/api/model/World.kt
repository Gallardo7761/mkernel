package net.miarma.mkernel.api.model

import org.bukkit.Bukkit
import org.bukkit.Location

data class World(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Int,
    val pitch: Int
) {
    fun toLocation(): Location {
        return Location(Bukkit.getWorld(name), x, y, z, yaw.toFloat(), pitch.toFloat())
    }
}
