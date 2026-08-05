package net.miarma.mkernel.api.model

data class Sequence(
    val name: String,
    val steps: List<CommandStep>
) {
    data class CommandStep(
        val command: String,
        val delayTicks: Long
    )
}
