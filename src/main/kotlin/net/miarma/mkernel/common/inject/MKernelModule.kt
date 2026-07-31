package net.miarma.mkernel.common.inject

import MKernel
import com.google.inject.AbstractModule

class MKernelModule(private val plugin: MKernel) : AbstractModule() {

    override fun configure() {
        bind(MKernel::class.java).toInstance(plugin)
    }
}