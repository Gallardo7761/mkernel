package net.miarma.mkernel.command.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil
import org.bukkit.Sound

@Singleton
class PayXpCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) {
    fun register() {
        commandAPICommand(configService.getString("commands.payxp.name")) {
            withArguments(
                PlayerProfileArgument(configService.getString("arguments.player")),
                IntegerArgument(configService.getString("arguments.levels"), 1)
            )
            withFullDescription(configService.getString("commands.payxp.description"))
            withPermission(configService.getString("commands.payxp.permission"))
            withUsage(configService.getString("commands.payxp.usage"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                val amount = args[1] as Int

                if (target == null) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (sender.level >= amount) {
                    sender.level -= amount
                    target.level += amount

                    messageService.builder(configService.getString("commands.payxp.messages.payYouOthers")).withPrefix().tag("target", target.name).tag("amount", amount.toString()).send(sender)
                    messageService.builder(configService.getString("commands.payxp.messages.payOthersYou")).withPrefix().forPlayer(target).tag("sender", sender.name).tag("amount", amount.toString()).send(target)

                    sender.playSound(sender.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                    target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                } else {
                    messageService.builder(configService.getString("language.errors.notEnoughLevels")).withPrefix().send(sender)
                }
            }
        }
    }
}