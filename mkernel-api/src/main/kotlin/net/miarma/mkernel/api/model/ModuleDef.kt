package net.miarma.mkernel.api.model

data class ModuleDef(
    val id: String,
    val namePath: String,
    val iconPath: String,
    val features: List<String>
)