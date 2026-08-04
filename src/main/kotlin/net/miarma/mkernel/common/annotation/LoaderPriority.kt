package net.miarma.mkernel.common.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoaderPriority(val priority: Int = 100) {
    companion object {
        const val HIGHEST = 0
        const val HIGH = 25
        const val NORMAL = 100
        const val LOW = 500
        const val LOWEST = 1000
    }
}