package net.miarma.mkernel.miarmacraft.inject

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import net.miarma.mkernel.api.model.ModuleDef
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys
import org.bukkit.Material

class MiarmacraftModule : AbstractModule() {
    override fun configure() {
        val moduleBinder = Multibinder.newSetBinder(binder(), ModuleDef::class.java)

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                ConfigKeys.Modules.BlindGod.MAIN,
                Material.SCULK,
                listOf()
            )
        )
    }
}