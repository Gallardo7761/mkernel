package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.inventory.DisposalInventory
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class DisposalCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val disposalInventory: DisposalInventory
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Disposal.NAME)) {
            withPermission(configService.getString(ConfigKeys.Commands.Disposal.PERM_BASE))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.Disposal.PERM_OTHERS))
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.Disposal.DESC))
            playerExecutor { sender, args ->
                if (args.count() == 0) {
                    disposalInventory.open(sender)
                } else {
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null || !target.isOnline) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                        return@playerExecutor
                    }
                    disposalInventory.open(target)
                }
            }
        }
    }
}