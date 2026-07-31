package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.ReferencingInventory
import xyz.xenondevs.invui.window.Window

@Singleton
class InvseeInventory @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {

    fun open(admin: Player, target: Player) {
        val inv = ReferencingInventory.fromPlayerStorageContents(target.inventory)

        val gui = Gui.builder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x"
            )
            .addIngredient('x', inv)
            .build()

        val title = messageService.builder(configService.getString("language.inventories.invsee.title"))
            .tag("player", target.name)
            .build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(admin)
            .build()
            .open()
    }
}