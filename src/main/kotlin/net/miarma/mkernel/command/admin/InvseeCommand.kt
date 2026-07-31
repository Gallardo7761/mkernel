package net.miarma.mkernel.command.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.common.inventory.InvseeInventory
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class InvseeCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val invseeInventory: InvseeInventory
) {
    fun register() {
        commandAPICommand(configService.getString("commands.invsee.name")) {
            withArguments(PlayerProfileArgument(configService.getString("arguments.player")))
            withShortDescription(configService.getString("commands.invsee.description"))
            withPermission(configService.getString("commands.invsee.permission"))
            withUsage(configService.getString("commands.invsee.usage"))
            playerExecutor { sender, args ->
                val target = PlayerUtil.fromArg(args[0])
                if (target == null || !target.isOnline) {
                    messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                    return@playerExecutor
                }
                invseeInventory.open(sender, target)
                messageService.builder(configService.getString("commands.invsee.messages.opened")).withPrefix().tag("player", target.name).send(sender)
            }
        }
    }
}