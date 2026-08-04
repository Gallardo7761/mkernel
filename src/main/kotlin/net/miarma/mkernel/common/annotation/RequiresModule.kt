package net.miarma.mkernel.common.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresModule(
    val module: String,
    val feature: String = ""
)
