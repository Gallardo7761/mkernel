package net.miarma.mkernel.common.model

import dev.dejvokep.boostedyaml.YamlDocument

data class Warp(
    val alias: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val world: String
) : Comparable<Warp> {

    fun toFormattedMessage(): String {
        val cx = x.toInt()
        val cy = y.toInt()
        val cz = z.toInt()

        return "<aqua><bold>$alias:</bold></aqua>\n" +
                "<click:copy_to_clipboard:'$cx $cy $cz'><hover:show_text:'<yellow>¡Haz click para copiar las coordenadas!</yellow>'><green>Coordenadas:</green> <white>$cx, $cy, $cz</white></hover></click>\n" +
                "<green>Mundo:</green> <white>$world</white>"
    }

    override fun compareTo(other: Warp): Int {
        return compareValuesBy(this, other, { it.x }, { it.y }, { it.z }, { it.world })
    }

    companion object {
        fun fromFile(c: YamlDocument, alias: String): Warp {
            return Warp(
                alias,
                c.getDouble("$alias.x"),
                c.getDouble("$alias.y"),
                c.getDouble("$alias.z"),
                c.getString("$alias.world")
            )
        }

        fun toFile(c: YamlDocument, warp: Warp) {
            c.set("${warp.alias}.x", warp.x)
            c.set("${warp.alias}.y", warp.y)
            c.set("${warp.alias}.z", warp.z)
            c.set("${warp.alias}.world", warp.world)
        }
    }
}
