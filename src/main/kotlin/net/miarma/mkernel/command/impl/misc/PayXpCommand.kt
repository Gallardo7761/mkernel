package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil
import org.bukkit.Sound

@Singleton
class PayXpCommand @Inject constructor(private val configService: ConfigService, private val messageService: MessageService) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.PayXp.NAME)) {
            withArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)),
                IntegerArgument(configService.getString(ConfigKeys.Arguments.LEVELS), 1)
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.PayXp.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.PayXp.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.PayXp.USAGE))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                val amount = args[1] as Int

                if (target == null) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }

                if (sender.level >= amount) {
                    sender.level -= amount
                    target.level += amount

                    messageService.builder(configService.getString(ConfigKeys.Commands.PayXp.MSG_YOU_OTHERS)).withPrefix().tag("target", target.name).tag("amount", amount.toString()).send(sender)
                    messageService.builder(configService.getString(ConfigKeys.Commands.PayXp.MSG_OTHERS_YOU)).withPrefix().forPlayer(target).tag("sender", sender.name).tag("amount", amount.toString()).send(target)

                    sender.playSound(sender.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
                    target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                } else {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.NOT_ENOUGH_LEVELS)).withPrefix().send(sender)
                }
            }
        }
    }
}