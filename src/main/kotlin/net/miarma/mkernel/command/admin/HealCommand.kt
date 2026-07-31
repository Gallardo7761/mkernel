package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class HealCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {
    fun register() {
        commandAPICommand(configService.getString("commands.heal.name")) {
            withPermission(configService.getString("commands.heal.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.heal.permissions.others"))
            )
            withFullDescription(configService.getString("commands.heal.description"))
            withUsage(configService.getString("commands.heal.usage"))
            playerExecutor { sender, args ->
                if (args[0] == null) {
                    sender.foodLevel = 20
                    sender.health = 20.0
                    sender.fireTicks = 0
                    messageService.builder(configService.getString("commands.heal.messages.healedSelf")).withPrefix().send(sender)
                    return@playerExecutor
                }

                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }

                target.foodLevel = 20
                target.health = 20.0
                target.fireTicks = 0

                messageService.builder(configService.getString("commands.heal.messages.healedPlayer")).withPrefix().tag("player", target.name).send(sender)
                messageService.builder(configService.getString("commands.heal.messages.beenHealed")).withPrefix().forPlayer(target).tag("sender", sender.name).send(target)
            }
        }
    }
}