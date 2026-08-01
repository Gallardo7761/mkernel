package net.miarma.mkernel.event

import com.google.inject.Inject
import com.google.inject.Singleton
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.PlayerUtil
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

        if (configService.isModuleEnabled("adminChat")) {
            val adminPerm = configService.getString("config.permissions.adminChat")
            val trigger = configService.getString("config.chat.admin.trigger")
            if (!trigger.isNullOrEmpty() && plainMessage.startsWith(trigger)) {
                event.isCancelled = true
                if (!player.hasPermission(adminPerm)) {
                    messageService.builder(configService.getString("language.errors.noPermission")).withPrefix().send(player)
                    return
                }
                val adminFormat = configService.getString("config.chat.admin.format")
                val rawMsg = plainMessage.substring(trigger.length).trim()
                val adminComponent = messageService.builder(adminFormat).forPlayer(player).tag("message", rawMsg).build()
                Bukkit.getOnlinePlayers().filter { it.hasPermission(adminPerm) }.forEach { it.sendMessage(adminComponent) }
                return
            }
        }

        if (configService.isModuleEnabled("endermanNWordAnger") &&
                SLUR_REGEX.containsMatchIn(plainMessage) &&
                PlayerUtil.isEntityNear(player, Enderman::class.java, 5)) {
            player.location.chunk.entities.filterIsInstance<Enderman>().forEach { enderman ->
                enderman.target = player
                enderman.isScreaming = true
                player.playSound(enderman.location, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 1.0f)
            }
        }

        if (configService.isModuleEnabled("mentions") && player.hasPermission(configService.getString("config.permissions.mentions"))) {
            Bukkit.getOnlinePlayers().forEach { target ->
                if (plainMessage.contains("@${target.name}")) {
                    val mentionFormat = configService.getString("language.events.onMention.format")
                    val mentionComponent = messageService.builder(mentionFormat).tag("player", target.name).build()
                    event.message(event.message().replaceText { config -> config.matchLiteral("@${target.name}").replacement(mentionComponent) })
                    messageService.builder(configService.getString("language.events.onMention.youWereMentioned")).withPrefix().forPlayer(target).tag("player", player.name).send(target)
                    target.playSound(target.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                }
            }
        }

        if (configService.isModuleEnabled("chatFormat") && player.hasPermission(configService.getString("config.permissions.chatFormat"))) {
            configService.getString("config.chat.public.format").takeIf { !it.isNullOrEmpty() }?.let { chatFormat ->
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
            messageService.builder(configService.getString("language.events.whileFrozen")).send(player)
            return
        }
        val commandMessage = event.message
        Bukkit.getOnlinePlayers().filter { p -> playerService.canSpy(p) && p != player }.forEach {
            messageService.builder(configService.getString("language.events.onCommand.spyMessage"))
                .tag("player", player.name)
                .tag("message", commandMessage)
                .send(it)
        }
    }
}
