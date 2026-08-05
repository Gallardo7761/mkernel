package net.miarma.mkernel.common.inject

import com.google.inject.AbstractModule
import net.miarma.mkernel.MKernel

class MKernelModule(private val plugin: MKernel) : AbstractModule() {

    override fun configure() {
        bind(MKernel::class.java).toInstance(plugin)
    }
}