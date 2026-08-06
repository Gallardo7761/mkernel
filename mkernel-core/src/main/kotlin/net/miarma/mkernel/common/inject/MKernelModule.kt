package net.miarma.mkernel.common.inject

import com.google.inject.AbstractModule
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.common.dao.*

class MKernelModule(private val plugin: MKernel) : AbstractModule() {

    override fun configure() {
        bind(MKernel::class.java).toInstance(plugin)
        bind(UserDao::class.java)
        bind(WorldDao::class.java)
        bind(HomeDao::class.java)
        bind(WarpDao::class.java)
        bind(InventoryDao::class.java)
        bind(TeleportDao::class.java)
        bind(ShopDao::class.java)
    }
}