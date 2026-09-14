package dev.br1ansouza.motoscope.simulator

import dev.br1ansouza.motoscope.protocol.elm327.ElmTransport
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

class SimulatedElmTransport(private val latencyMillis: Long = DEFAULT_LATENCY_MILLIS) :
    ElmTransport {
    private val pending = Channel<String>(Channel.UNLIMITED)

    override suspend fun write(text: String) {
        val command = text.trim().uppercase()
        delay(latencyMillis)
        pending.send(answer(command) + PROMPT)
    }

    override suspend fun read(): String = pending.receive()

    private fun answer(command: String): String = when {
        command == "ATZ" -> "ELM327 v1.5$SEPARATOR"
        command == "ATDP" -> "AUTO, ISO 15765-4 (CAN 11/500)$SEPARATOR"
        command == "ATRV" -> "12.4V$SEPARATOR"
        command.startsWith("AT") -> "OK$SEPARATOR"
        command.startsWith("01") -> currentData(command.drop(2))
        else -> "NO DATA$SEPARATOR"
    }

    private fun currentData(pid: String): String {
        val mask = SUPPORT_MASKS[pid] ?: return "NO DATA$SEPARATOR"
        return "41$pid$mask$SEPARATOR"
    }

    private companion object {
        const val DEFAULT_LATENCY_MILLIS = 40L
        const val SEPARATOR = "\r"
        const val PROMPT = ">"

        val SUPPORT_MASKS = mapOf(
            "00" to "BE1FA813",
            "20" to "8000A001",
            "40" to "7A1AA000"
        )
    }
}
