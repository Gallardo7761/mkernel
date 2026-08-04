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
@RequiresModule(ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SPY)
class SpyCommand @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val playerService: PlayerService,
    private val moduleLoader: ModuleLoader
) : MCommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Spy.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Admin.MAIN, ConfigKeys.Modules.Admin.Commands.SPY)
            withFullDescription(configService.getString(ConfigKeys.Commands.Spy.DESC))
            withPermission(configService.getString(ConfigKeys.Commands.Spy.PERM))
            playerExecutor { sender, _ ->
                if (playerService.canSpy(sender)) {
                    playerService.setSpy(sender, false)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Spy.MSG_DISABLED)).withPrefix().send(sender)
                } else {
                    playerService.setSpy(sender, true)
                    messageService.builder(configService.getString(ConfigKeys.Commands.Spy.MSG_ENABLED)).withPrefix().send(sender)
                }
            }
        }
    }
}