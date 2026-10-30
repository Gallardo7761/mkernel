package es.killobyte.nucleo.common.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import es.killobyte.nucleo.NucleoPlugin
import es.killobyte.nucleo.api.annotation.LoaderPriority
import es.killobyte.nucleo.api.common.IService
import es.killobyte.nucleo.common.dao.InventoryDao
import xyz.xenondevs.invui.inventory.VirtualInventory

@Singleton
@LoaderPriority(LoaderPriority.LOWEST)
class GlobalChestService @Inject constructor(
    private val plugin: NucleoPlugin,
    private val inventoryDao: InventoryDao
) : IService {

    lateinit var inventory: VirtualInventory
        private set

    companion object {
        private const val INVENTORY_ID = "global_chest"
    }

    override fun onEnable() {
        plugin.launchSync {
            val bin = inventoryDao.loadInventoryBytes(INVENTORY_ID)

            inventory = if (bin != null && bin.isNotEmpty()) {
                try {
                    VirtualInventory.deserialize(bin).also {
                        plugin.logger.info("Global chest loaded from database successfully!")
                    }
                } catch (e: Exception) {
                    plugin.logger.warning("Global chest data corrupted, creating a fresh inventory.")
                    VirtualInventory(54)
                }
            } else {
                VirtualInventory(54)
            }
        }
    }

    override fun onDisable() {
        if (::inventory.isInitialized) {
            plugin.launchAsync {
                inventoryDao.saveInventoryBytes(INVENTORY_ID, inventory.serialize())
                plugin.logger.info("Global chest saved to database successfully.")
            }
        }
    }
}