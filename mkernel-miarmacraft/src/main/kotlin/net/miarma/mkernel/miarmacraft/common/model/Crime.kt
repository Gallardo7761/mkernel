package net.miarma.mkernel.miarmacraft.common.model

data class Crime (
    val id: Int,
    val crime: String,
    val timestamp: Long,
    val count: Int
)