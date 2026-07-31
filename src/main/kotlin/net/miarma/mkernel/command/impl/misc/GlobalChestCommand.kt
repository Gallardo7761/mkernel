package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.inventory.GlobalChestInventory
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.PlayerUtil

@Singleton
class GlobalChestCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val globalChestInventory: GlobalChestInventory
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString("commands.globalchest.name")) {
            withPermission(configService.getString("commands.globalchest.permissions.base"))
            withAliases(*configService.getStringList("commands.globalchest.aliases").toTypedArray())
            withOptionalArguments(
                PlayerProfileArgument(configService.getString("arguments.player"))
                    .withPermission(configService.getString("commands.globalchest.permissions.others"))
            )
            withFullDescription(configService.getString("commands.globalchest.description"))
            playerExecutor { sender, args ->
                if (args.count() == 0) {
                    globalChestInventory.open(sender)
                } else {
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null || !target.isOnline) {
                        messageService.builder(configService.getString("language.errors.playerNotFound")).withPrefix().send(sender)
                        return@playerExecutor
                    }
                    globalChestInventory.open(target)
                }
            }
        }
    }
}