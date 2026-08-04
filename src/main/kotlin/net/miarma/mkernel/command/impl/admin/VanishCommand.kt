package net.miarma.mkernel.command.impl.admin

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.command.MCommand
import net.miarma.mkernel.common.annotation.RequiresModule
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.common.service.impl.PlayerService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.VANISH)
class VanishCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Vanish.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.VANISH)
            withAliases("v")
            withFullDescription(configService.getString(ConfigKeys.Commands.Vanish.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Vanish.PERM))
            playerExecutor { sender, _ ->
                if (playerService.isVanished(sender)) {
                    playerService.setVanished(sender, false)
                    sender.isInvisible = false
                    sender.canPickupItems = true
                    messageService.builder(configService.getString(ConfigKeys.Commands.Vanish.MSG_UNVANISHED)).withPrefix().send(sender)
                } else {
                    playerService.setVanished(sender, true)
                    sender.isInvisible = true
                    sender.canPickupItems = false
                    messageService.builder(configService.getString(ConfigKeys.Commands.Vanish.MSG_VANISHED)).withPrefix().send(sender)
                }
            }
        }
    }
}