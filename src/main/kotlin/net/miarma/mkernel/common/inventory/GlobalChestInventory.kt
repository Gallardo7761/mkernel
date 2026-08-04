package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.GlobalChestService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

@Singleton
class GlobalChestInventory @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService,
    private val globalChestService: GlobalChestService
) {

    fun open(player: Player) {
        val title = messageService.builder(configService.getString(ConfigKeys.Messages.Inventories.GLOBAL_CHEST_TITLE)).build()

        val gui = Gui.builder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x"
            )
            .addIngredient('x', globalChestService.inventory)
            .build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }
}