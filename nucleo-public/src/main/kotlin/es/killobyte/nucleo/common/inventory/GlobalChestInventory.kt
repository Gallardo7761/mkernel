package es.killobyte.nucleo.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.common.config.ConfigKeys
import es.killobyte.nucleo.common.service.impl.ConfigService
import es.killobyte.nucleo.common.service.impl.GlobalChestService
import es.killobyte.nucleo.common.service.impl.MessageService
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