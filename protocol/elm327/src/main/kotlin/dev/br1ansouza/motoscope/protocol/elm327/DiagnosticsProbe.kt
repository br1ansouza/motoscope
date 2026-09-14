package dev.br1ansouza.motoscope.protocol.elm327

data class DiagnosticsReport(
    val handshake: ElmHandshakeResult,
    val protocol: String?,
    val adapterVoltage: String?,
    val ranges: List<SupportedPidRange>,
    val discardedFrames: Long
) {
    val supportedPids: List<Int> = ranges.flatMap { it.pids }.distinct().sorted()
}

class DiagnosticsProbe(
    private val queue: ElmCommandQueue,
    private val handshake: ElmHandshake = ElmHandshake(queue)
) {
    suspend fun run(): DiagnosticsReport {
        val result = handshake.run()
        if (!result.completed) {
            return DiagnosticsReport(
                handshake = result,
                protocol = null,
                adapterVoltage = null,
                ranges = emptyList(),
                discardedFrames = queue.discardedFrames
            )
        }
        return DiagnosticsReport(
            handshake = result,
            protocol = result.textOf(ElmCommand.DESCRIBE_PROTOCOL),
            adapterVoltage = readText(ElmCommand.READ_ADAPTER_VOLTAGE),
            ranges = scanRanges(),
            discardedFrames = queue.discardedFrames
        )
    }

    private suspend fun readText(command: ElmCommand): String? {
        val exchange = queue.execute(command) as? ElmExchange.Completed ?: return null
        return (exchange.response as? ElmResponse.Text)?.value
    }

    private suspend fun scanRanges(): List<SupportedPidRange> {
        val ranges = mutableListOf<SupportedPidRange>()
        for (base in SupportedPids.PROBE_ORDER) {
            val keepGoing = ranges.isEmpty() || ranges.last().nextRangeSupported
            val range = if (keepGoing) readRange(base) else null
            if (range == null) break
            ranges += range
        }
        return ranges
    }

    private suspend fun readRange(base: Int): SupportedPidRange? {
        val request = ElmCommand.ObdRequest(CURRENT_DATA, base)
        val data = (queue.execute(request) as? ElmExchange.Completed)
            ?.response
            ?.let { it as? ElmResponse.Data }
        val payload = data?.let { ObdResponseReader.read(request, it) }
        return payload?.let { SupportedPids.decode(base, it.bytes) }
    }

    private fun ElmHandshakeResult.textOf(command: ElmCommand): String? = steps
        .lastOrNull { it.command == command }
        ?.exchange
        ?.let { it as? ElmExchange.Completed }
        ?.response
        ?.let { it as? ElmResponse.Text }
        ?.value

    private companion object {
        const val CURRENT_DATA = 0x01
    }
}
