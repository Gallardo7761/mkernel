package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
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
        commandAPICommand(configService.getString("commands.disposal.name")) {
            withPermission(configService.getString("commands.disposal.permissions.base"))
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.disposal.permissions.others"))
            )
            withFullDescription(configService.getString("commands.disposal.description"))
            playerExecutor { sender, args ->
                if (args.count() == 0) {
                    disposalInventory.open(sender)
                } else {
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null || !target.isOnline) {
                        messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                        return@playerExecutor
                    }
                    disposalInventory.open(target)
                }
            }
        }
    }
}