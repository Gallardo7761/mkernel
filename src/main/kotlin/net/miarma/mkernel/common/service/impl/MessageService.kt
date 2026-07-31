package net.miarma.mkernel.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.miarma.mkernel.common.service.IService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Singleton
class MessageService @Inject constructor(private val configService: ConfigService) : IService {

    companion object {
        private val MINI_MESSAGE = MiniMessage.miniMessage()
        private val SAFE_MINI_MESSAGE = MiniMessage.builder().tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.rainbow())
                .build()
        ).build()
    }

    fun builder(text: String) = Builder(text)

    inner class Builder(private val text: String?) {
        private var player: Player? = null
        private var usePrefix = false
        private val resolvers = mutableListOf<TagResolver>()

        fun withPrefix() = apply { usePrefix = true }
        fun forPlayer(player: Player) = apply { this.player = player }
        fun tag(name: String, value: String) = apply { resolvers.add(Placeholder.parsed(name, value)) }
        fun componentTag(name: String, component: Component) = apply { resolvers.add(Placeholder.component(name, component)) }

        fun build(): Component {
            if (text.isNullOrEmpty()) return Component.empty()

            var fullText = if (usePrefix) {
                "${configService.getString("config.chat.prefix")} $text"
            } else {
                text
            }

            player?.let {
                fullText = PlaceholderAPI.setPlaceholders(it, fullText)
            }

            return MINI_MESSAGE.deserialize(fullText, TagResolver.resolver(resolvers))
        }

        fun send(sender: CommandSender?) {
            sender ?: return
            if (player == null && sender is Player) {
                player = sender
            }
            sender.sendMessage(build())
        }
    }

    fun parsePlayerMessage(text: String, player: Player?): Component {
        val placeholderText = player?.let { PlaceholderAPI.setPlaceholders(it, text) } ?: text
        return SAFE_MINI_MESSAGE.deserialize(placeholderText)
    }

    fun stripColors(message: String?): String {
        return if (message.isNullOrEmpty()) "" else PlainTextComponentSerializer.plainText().serialize(MINI_MESSAGE.deserialize(message))
    }
}
