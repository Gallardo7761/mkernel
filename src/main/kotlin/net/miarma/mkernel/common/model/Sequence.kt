package net.miarma.mkernel.common.model

data class Sequence(
    val name: String,
    val steps: List<CommandStep>
) {
    data class CommandStep(
        val command: String,
        val delayTicks: Long
    )
}
