package net.miarma.mkernel.common.service.impl

import MKernel // <-- Necesitamos importar el plugin pa' las corrutinas
import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.service.IService
import xyz.xenondevs.invui.inventory.VirtualInventory

@Singleton
class GlobalChestService @Inject constructor(
    private val plugin: MKernel,
    private val databaseService: DatabaseService
) : IService {

    lateinit var inventory: VirtualInventory
        private set

    companion object {
        private const val INVENTORY_ID = "global_chest"
    }

    override fun onEnable() {
        plugin.launchSync {
            val bin = databaseService.loadInventoryBytes(INVENTORY_ID)

            inventory = if (bin != null && bin.isNotEmpty()) {
                try {
                    VirtualInventory.deserialize(bin).also {
                        MKernel.LOGGER.info("Global chest loaded from database successfully!")
                    }
                } catch (e: Exception) {
                    MKernel.LOGGER.warning("Global chest data corrupted, creating a fresh inventory.")
                    VirtualInventory(54)
                }
            } else {
                VirtualInventory(54)
            }
        }
    }

    override fun onDisable() {
        if (::inventory.isInitialized) {
            databaseService.saveInventoryBytesSync(INVENTORY_ID, inventory.serialize())
            MKernel.LOGGER.info("Global chest saved to database successfully.")
        }
    }
}