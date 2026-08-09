package net.miarma.mkernel.miarmacraft.common.model

import java.sql.Timestamp

data class Crime(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val description: String,
    val fineAmount: Double,
    val createdAt: Timestamp
)