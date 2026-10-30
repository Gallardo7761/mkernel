package es.killobyte.nucleo.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import es.killobyte.nucleo.api.annotation.RequiresModule
import es.killobyte.nucleo.api.common.ICommand
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.inventory.GlobalChestInventory
import es.killobyte.nucleo.common.module.ModuleLoader
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.MessageService
import es.killobyte.nucleo.util.CommandUtil.checkModule
import es.killobyte.nucleo.util.PlayerUtil

@Singleton
@RequiresModule(ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.GLOBAL_CHEST)
class GlobalChestCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val globalChestInventory: GlobalChestInventory,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.GlobalChest.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Core.MAIN, ConfigKeys.Modules.Core.Commands.GLOBAL_CHEST)
            withPermission(configService.getString(ConfigKeys.Commands.GlobalChest.PERM_BASE))
            withAliases(*configService.getStringList(ConfigKeys.Commands.GlobalChest.ALIASES).toTypedArray())
            withOptionalArguments(
                PlayerProfileArgument(configService.getString(ConfigKeys.Arguments.PLAYER))
                    .withPermission(configService.getString(ConfigKeys.Commands.GlobalChest.PERM_OTHERS))
            )
            withFullDescription(configService.getString(ConfigKeys.Commands.GlobalChest.DESC))
            playerExecutor { sender, args ->
                if (args.count() == 0) {
                    globalChestInventory.open(sender)
                } else {
                    val target = PlayerUtil.fromArg(args[0])
                    if (target == null || !target.isOnline) {
                        messageService.builder(configService.getString(ConfigKeys.Messages.General.Errors.PLAYER_NOT_FOUND)).withPrefix().send(sender)
                        return@playerExecutor
                    }
                    globalChestInventory.open(target)
                }
            }
        }
    }
}