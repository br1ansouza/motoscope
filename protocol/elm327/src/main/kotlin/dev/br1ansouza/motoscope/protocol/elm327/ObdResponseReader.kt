package dev.br1ansouza.motoscope.protocol.elm327

data class ObdPayload(val mode: Int, val pid: Int?, val bytes: List<Int>)

object ObdResponseReader {
    fun read(request: ElmCommand.ObdRequest, response: ElmResponse.Data): ObdPayload? {
        val bytes = response.bytes
        val pid = request.pid
        val payload = when {
            bytes.firstOrNull() != request.mode + POSITIVE_RESPONSE_OFFSET -> null
            pid == null -> bytes.drop(1)
            bytes.getOrNull(1) != pid -> null
            else -> bytes.drop(2)
        }
        return if (payload.isNullOrEmpty()) null else ObdPayload(request.mode, pid, payload)
    }

    private const val POSITIVE_RESPONSE_OFFSET = 0x40
}
