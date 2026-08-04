package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.inventory.InvseeInventory
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class InvseeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val invseeInventory: InvseeInventory
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Invsee.NAME)) {
            withArguments(PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER)))
            withShortDescription(configService.getString(ConfigKeys.Commands.Invsee.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Invsee.PERM))
            withUsage(configService.getString(ConfigKeys.Commands.Invsee.USAGE))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                    return@playerExecutor
                }
                invseeInventory.open(sender, target)
                messageService.builder(configService.getString(ConfigKeys.Commands.Invsee.MSG_OPENED)).withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}