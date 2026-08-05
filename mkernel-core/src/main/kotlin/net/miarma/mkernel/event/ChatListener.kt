package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil
import net.miarma.mkernel.util.PlayerUtil.getNickName
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Enderman
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

@Singleton
class ChatListener @Inject constructor(
    private val configService: ConfigService,
    private val playerService: PlayerService,
    private val messageService: MessageService
) : Listener {

    companion object {
        private val SLUR_REGEX = Regex("\\bnigg(a|er|as|ers)?\\b", RegexOption.IGNORE_CASE)
    }

    @EventHandler
    fun onChatMessage(event: AsyncChatEvent) {
        val player = event.player
        val plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message())

        if (configService.isModuleEnabled(ConfigKeys.Modules.Admin.CHAT)) {
            val adminPerm = configService.getString(ConfigKeys.Settings.Chat.Admin.PERM)
            val trigger = configService.getString(ConfigKeys.Settings.Chat.Admin.TRIGGER)
            if (!trigger.isNullOrEmpty() && plainMessage.startsWith(trigger)) {
                event.isCancelled = true
                if (!player.hasPermission(adminPerm)) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.NO_PERMISSION)).withPrefix().send(player)
                    return
                }
                val adminFormat = configService.getString(ConfigKeys.Settings.Chat.Admin.FORMAT)
                val rawMsg = plainMessage.substring(trigger.length).trim()
                val adminComponent = messageService.builder(adminFormat).forPlayer(player).tag("message", rawMsg).build()
                Bukkit.getOnlinePlayers().filter { it.hasPermission(adminPerm) }.forEach { it.sendMessage(adminComponent) }
                return
            }
        }

        val normalized = plainMessage.lowercase().replace(Regex("[^a-z]"), "")
        if (configService.isModuleEnabled(ConfigKeys.Modules.Chat.ENDERMAN_ANGER) &&
                SLUR_REGEX.containsMatchIn(normalized) &&
                PlayerUtil.isEntityNear(player, Enderman::class.java, 5)) {
            player.location.chunk.entities.filterIsInstance<Enderman>().forEach { enderman ->
                enderman.target = player
                enderman.isScreaming = true
                player.playSound(enderman.location, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 1.0f)
            }
        }

        if (configService.isModuleEnabled(ConfigKeys.Modules.Chat.MENTIONS) &&
                player.hasPermission(configService.getString(ConfigKeys.Settings.Chat.PERM_MENTIONS))) {
            Bukkit.getOnlinePlayers().forEach { target ->
                val targetNick = target.getNickName(playerService)
                val mentionedByName = plainMessage.contains("@${target.name}")
                val hasCustomNick = targetNick != target.name
                val mentionedByNick = hasCustomNick && plainMessage.contains("@$targetNick")

                if (mentionedByName || mentionedByNick) {
                    val mentionFormat = configService.getString(ConfigKeys.Messages.Chat.MENTIONS_FORMAT)
                    val mentionComponent = messageService.builder(mentionFormat).tag("player", targetNick).build()
                    var newMessage = event.message()

                    if (mentionedByName) {
                        newMessage = newMessage.replaceText { config ->
                            config.matchLiteral("@${target.name}").replacement(mentionComponent)
                        }
                    }

                    if (mentionedByNick) {
                        newMessage = newMessage.replaceText { config ->
                            config.matchLiteral("@$targetNick").replacement(mentionComponent)
                        }
                    }

                    event.message(newMessage)

                    val senderNick = player.getNickName(playerService)

                    messageService.builder(configService.getString(ConfigKeys.Messages.Chat.YOU_WERE_MENTIONED))
                        .withPrefix().forPlayer(target).tag("player", senderNick).send(target)

                    target.playSound(target.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                }
            }
        }

        if (configService.isModuleEnabled(ConfigKeys.Modules.Chat.FORMAT) &&
                player.hasPermission(configService.getString(ConfigKeys.Settings.Chat.PERM_FORMAT))) {
            configService.getString(ConfigKeys.Settings.Chat.PUBLIC_FORMAT).takeIf { !it.isNullOrEmpty() }?.let { chatFormat ->
                event.renderer { source, _, message, _ ->
                    messageService.builder(chatFormat)
                        .forPlayer(source)
                        .componentTag("message", messageService.parsePlayerMessage(PlainTextComponentSerializer.plainText().serialize(message), player))
                        .build()
                }
            }
        }
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (playerService.isFrozen(player)) {
            event.isCancelled = true
            messageService.builder(configService.getString(ConfigKeys.Messages.Admin.WHILE_FROZEN)).send(player)
            return
        }
        val commandMessage = event.message
        Bukkit.getOnlinePlayers().filter { p -> playerService.canSpy(p) && p != player }.forEach {
            messageService.builder(configService.getString(ConfigKeys.Messages.Admin.SPY_MESSAGE))
                .tag("player", player.getNickName(playerService))
                .tag("message", commandMessage)
                .send(it)
        }
    }
}
