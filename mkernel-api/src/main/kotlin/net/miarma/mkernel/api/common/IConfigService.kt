package net.miarma.mkernel.api.common

interface IConfigService {
    fun getBoolean(path: String, def: Boolean = false): Boolean
    fun getString(path: String, def: String = ""): String
    fun getInt(path: String, def: Int = 0): Int
    fun getDouble(path: String, def: Double = 0.0): Double
}