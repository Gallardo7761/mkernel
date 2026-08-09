package net.miarma.mkernel.miarmacraft.common.model

import java.sql.Timestamp
import java.util.*

data class CrimeHistory(
    val id: Int,
    val userUuid: UUID,
    val crimeId: Int,
    val officerUuid: UUID,
    val createdAt: Timestamp,
    val status: CrimeStatus,
    val crimeName: String,
    val crimeDescription: String,
    val categoryName: String
) {
    enum class CrimeStatus {
        PENDING,
        PAID,
        FORGIVEN
    }
}