package dev.br1ansouza.motoscope.protocol.elm327

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

class ElmCommandQueue(
    private val transport: ElmTransport,
    private val timeoutMillis: Long,
    private val maxAttempts: Int,
    private val maxSearchingFrames: Int
) {
    init {
        require(timeoutMillis > 0) {
            "Timeout precisa ser positivo, recebido $timeoutMillis."
        }
        require(maxAttempts >= 1) {
            "Número de tentativas precisa ser ao menos 1, recebido $maxAttempts."
        }
        require(maxSearchingFrames >= 0) {
            "Limite de quadros SEARCHING não pode ser negativo, recebido $maxSearchingFrames."
        }
    }

    private val mutex = Mutex()
    private val accumulator = ElmFrameAccumulator()
    private val ready = ArrayDeque<String>()

    var discardedFrames: Long = 0
        private set

    suspend fun execute(command: ElmCommand): ElmExchange = mutex.withLock {
        repeat(maxAttempts) { index ->
            discardPending()
            transport.write(command.text + TERMINATOR)
            val response = awaitResponse(command.text)
            if (response != null) return ElmExchange.Completed(response, index + 1)
        }
        ElmExchange.TimedOut(maxAttempts)
    }

    private suspend fun awaitResponse(sentCommand: String): ElmResponse? {
        var searching = 0
        var settled: ElmResponse? = null
        while (settled == null) {
            val frame = nextFrame() ?: break
            val response = ElmResponseParser.parse(frame, sentCommand)
            if (response !is ElmResponse.Searching || searching >= maxSearchingFrames) {
                settled = response
            } else {
                searching++
            }
        }
        return settled
    }

    private suspend fun nextFrame(): String? = withTimeoutOrNull(timeoutMillis) {
        var frame = ready.removeFirstOrNull()
        while (frame == null) {
            ready.addAll(accumulator.append(transport.read()))
            frame = ready.removeFirstOrNull()
        }
        frame
    }

    private fun discardPending() {
        val orphans = ready.size + if (accumulator.pending().isNotBlank()) 1 else 0
        ready.clear()
        accumulator.reset()
        discardedFrames += orphans
    }

    private companion object {
        const val TERMINATOR = "\r"
    }
}
