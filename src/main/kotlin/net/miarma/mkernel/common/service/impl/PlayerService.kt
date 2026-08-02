package net.miarma.mkernel.common.service.impl

import MKernel
import com.google.inject.Inject
import com.google.inject.Singleton
import net.kyori.adventure.text.Component
import net.miarma.mkernel.common.service.IService
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

@Singleton
class PlayerService @Inject constructor(plugin: MKernel) : IService {

    private val vanishKey = NamespacedKey(plugin, "vanish")
    private val spyKey = NamespacedKey(plugin, "spy")
    private val frozenKey = NamespacedKey(plugin, "frozen")
    private val nickKey = NamespacedKey(plugin, "nickname")

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

    fun getNick(player: Player): String? {
        return player.persistentDataContainer.get(nickKey, PersistentDataType.STRING)
    }

    fun setNick(player: Player, nick: String?) {
        if (nick == null) {
            player.persistentDataContainer.remove(nickKey)
            player.playerListName(Component.text(player.name))
        } else {
            player.persistentDataContainer.set(nickKey, PersistentDataType.STRING, nick)
        }
    }
}
