package net.miarma.mkernel.common.inventory

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.impl.ConfigService
import net.miarma.mkernel.common.service.impl.MessageService
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.window.Window

@Singleton
class DisposalInventory @Inject constructor(
    private val configService: ConfigService,
    private val messageService: MessageService
) {

    fun open(player: Player) {
        val disposalInv = VirtualInventory(54)

        disposalInv.addPostUpdateHandler { event ->
            if (event.isAdd || event.isSwap) {
                event.inventory.setItem(UpdateReason.SUPPRESSED, event.slot, null)
            }
        }

        val title = messageService.builder(configService.getString("language.inventories.disposal.title")).build()

        val gui = Gui.builder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x"
            )
            .addIngredient('x', disposalInv)
            .build()

        Window.builder()
            .setTitle(title)
            .setUpperGui(gui)
            .setViewer(player)
            .build()
            .open()
    }
}