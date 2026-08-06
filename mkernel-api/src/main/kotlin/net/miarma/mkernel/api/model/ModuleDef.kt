package net.miarma.mkernel.api.model

import org.bukkit.Material

data class ModuleDef(val id: String, val icon: Material, val features: List<String>)