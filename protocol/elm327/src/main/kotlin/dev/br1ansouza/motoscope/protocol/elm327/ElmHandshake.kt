package dev.br1ansouza.motoscope.protocol.elm327

data class ElmHandshakeStep(val command: ElmCommand, val exchange: ElmExchange)

data class ElmHandshakeResult(val steps: List<ElmHandshakeStep>, val completed: Boolean) {
    val failedAt: ElmHandshakeStep? = if (completed) null else steps.lastOrNull()
}

class ElmHandshake(private val queue: ElmCommandQueue) {
    suspend fun run(commands: List<ElmCommand> = ElmInitialization.DEFAULT): ElmHandshakeResult {
        val steps = mutableListOf<ElmHandshakeStep>()
        for (command in commands) {
            val exchange = queue.execute(command)
            steps += ElmHandshakeStep(command, exchange)
            if (!exchange.accepted()) return ElmHandshakeResult(steps, completed = false)
        }
        return ElmHandshakeResult(steps, completed = true)
    }

    private fun ElmExchange.accepted(): Boolean = when (this) {
        is ElmExchange.TimedOut -> false
        is ElmExchange.Completed -> when (response) {
            is ElmResponse.Ok, is ElmResponse.Text, is ElmResponse.Data -> true
            is ElmResponse.NoData, is ElmResponse.Searching -> false
            is ElmResponse.Failure, is ElmResponse.Unhandled -> false
        }
    }
}
