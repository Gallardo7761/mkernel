package net.miarma.mkernel.common.model

import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*

data class Shop(
    val id: String,
    val ownerUuid: UUID,
    val location: Location,
    var item: ItemStack,
    val price: Double,
    var stock: Int
)
