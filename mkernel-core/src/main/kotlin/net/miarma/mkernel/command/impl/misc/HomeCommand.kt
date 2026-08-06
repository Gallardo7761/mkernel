package net.miarma.mkernel.command.impl.misc

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.miarma.mkernel.api.annotation.RequiresModule
import net.miarma.mkernel.api.common.ICommand
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.dao.HomeDao
import net.miarma.mkernel.common.module.ModuleLoader
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import net.miarma.mkernel.util.CommandUtil.checkModule

@Singleton
@RequiresModule(ConfigKeys.Modules.Teleport.MAIN)
class HomeCommand @Inject constructor(
    private val configService: ConfigService,
    private val homeDao: HomeDao,
    private val messageService: MessageService,
    private val moduleLoader: ModuleLoader
) : ICommand {
    override fun register() {
        commandAPICommand(configService.getString(ConfigKeys.Commands.Home.NAME)) {
            checkModule(moduleLoader, configService, ConfigKeys.Modules.Teleport.MAIN)
            withPermission(configService.getString(ConfigKeys.Commands.Home.PERM))
            withFullDescription(configService.getString(ConfigKeys.Commands.Home.DESC))
            playerExecutor { sender, _ ->
                val loc = homeDao.getHome(sender)

                if (loc != null) {
                    sender.teleportAsync(loc).thenAccept { success ->
                        if (success) {
                            messageService.builder(configService.getString(ConfigKeys.Commands.Home.MSG_TELEPORTED))
                                .withPrefix().send(sender)
                        }
                    }
                } else {
                    messageService.builder(configService.getString(ConfigKeys.Commands.Home.MSG_NOT_EXIST))
                        .withPrefix().send(sender)
                }
            }
        }
    }
}