package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.IService
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

@Singleton
class PlayerService @Inject constructor(plugin: MKernel) : IService {

    private val vanishKey = NamespacedKey(plugin, "vanish")
    private val spyKey = NamespacedKey(plugin, "spy")
    private val frozenKey = NamespacedKey(plugin, "frozen")

    fun isFrozen(player: Player): Boolean {
        return player.persistentDataContainer.get(frozenKey, PersistentDataType.BOOLEAN) ?: false
    }

    fun isVanished(player: Player): Boolean {
        return player.persistentDataContainer.get(vanishKey, PersistentDataType.BOOLEAN) ?: false
    }

    fun canSpy(player: Player): Boolean {
        return player.persistentDataContainer.get(spyKey, PersistentDataType.BOOLEAN) ?: false
    }

    fun setFrozen(player: Player, value: Boolean) {
        player.persistentDataContainer.set(frozenKey, PersistentDataType.BOOLEAN, value)
    }

    fun setVanished(player: Player, value: Boolean) {
        player.persistentDataContainer.set(vanishKey, PersistentDataType.BOOLEAN, value)
    }

    fun setSpy(player: Player, value: Boolean) {
        player.persistentDataContainer.set(spyKey, PersistentDataType.BOOLEAN, value)
    }
}
