package es.killobyte.nucleo.api.model

data class ModuleDef(
    val id: String,
    val namePath: String,
    val iconPath: String,
    val features: List<String>
)