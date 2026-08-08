package net.miarma.mkernel.miarmacraft.common.model

data class Crime(
    val id: Int,
    val crime: String,
    val timestamp: Long,
    val count: Int,
    val status: CrimeStatus = CrimeStatus.PENDING
) {
    enum class CrimeStatus {
        PENDING, CLEARED
    }
}