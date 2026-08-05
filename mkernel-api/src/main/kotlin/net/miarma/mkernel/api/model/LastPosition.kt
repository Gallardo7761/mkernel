package net.miarma.mkernel.api.model

import org.bukkit.Location
import java.util.concurrent.atomic.AtomicReference

class LastPosition {
    private val lastPosition = AtomicReference<Location?>()

    fun set(location: Location?) {
        this.lastPosition.set(location?.clone())
    }

    fun get(): Location? {
        return this.lastPosition.get()?.clone()
    }

    fun getAndSet(location: Location?): Location? {
        val newLoc = location?.clone()
        return this.lastPosition.getAndSet(newLoc)?.clone()
    }

    fun hasPosition(): Boolean {
        return this.lastPosition.get() != null
    }

    fun clear() {
        this.lastPosition.set(null)
    }
}
