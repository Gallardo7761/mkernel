package net.miarma.mkernel.miarmacraft.common.inject

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import net.miarma.mkernel.api.model.ModuleDef
import net.miarma.mkernel.miarmacraft.common.config.ConfigKeys

class MiarmacraftModule : AbstractModule() {
    override fun configure() {
        val moduleBinder = Multibinder.newSetBinder(binder(), ModuleDef::class.java)

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "blindGod",
                ConfigKeys.Messages.BlindGod._Meta.NAME,
                ConfigKeys.Messages.BlindGod._Meta.ICON,
                emptyList()
            )
        )

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "lawEnforcement",
                ConfigKeys.Messages.LawEnforcement._Meta.NAME,
                ConfigKeys.Messages.LawEnforcement._Meta.ICON,
                emptyList()
            )
        )

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "shit",
                ConfigKeys.Messages.Shit._Meta.NAME,
                ConfigKeys.Messages.Shit._Meta.ICON,
                emptyList()
            )
        )

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "paysheet",
                ConfigKeys.Messages.Paysheet._Meta.NAME,
                ConfigKeys.Messages.Paysheet._Meta.ICON,
                emptyList()
            )
        )

        moduleBinder.addBinding().toInstance(
            ModuleDef(
                "piglinCartel",
                ConfigKeys.Messages.PiglinCartel._Meta.NAME,
                ConfigKeys.Messages.PiglinCartel._Meta.ICON,
                emptyList()
            )
        )
    }
}