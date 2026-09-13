package dev.br1ansouza.motoscope.protocol.elm327

object ElmResponseParser {
    fun parse(frame: String, sentCommand: String? = null): ElmResponse {
        val lines = meaningfulLines(frame, sentCommand)
        val failure = failureIn(lines)
        return when {
            lines.isEmpty() -> ElmResponse.Unhandled(frame)
            failure != null -> failure
            else -> classify(frame, lines)
        }
    }

    private fun classify(frame: String, lines: List<String>): ElmResponse {
        val payloadLines = lines.filterNot { it.isTransientStatus() }
        return when {
            payloadLines.isEmpty() && lines.any { it.matches(SEARCHING) } -> ElmResponse.Searching
            payloadLines.isEmpty() -> ElmResponse.Ok
            payloadLines.size > 1 -> ElmResponse.Unhandled(frame)
            payloadLines.single().contains(':') -> ElmResponse.Unhandled(frame)
            else -> payloadLines.single().asDataOrText()
        }
    }

    private fun meaningfulLines(frame: String, sentCommand: String?): List<String> {
        val echo = sentCommand?.normalized()
        return frame.split('\r', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { echo != null && it.normalized() == echo }
    }

    private fun failureIn(lines: List<String>): ElmResponse.Failure? {
        val kind = lines.firstNotNullOfOrNull { it.failureKind() } ?: return null
        return ElmResponse.Failure(kind, lines.joinToString(separator = " "))
    }

    private fun String.failureKind(): ElmFailure? {
        val text = uppercase()
        return when {
            text == UNKNOWN -> ElmFailure.UNKNOWN_COMMAND
            text.contains("UNABLE TO CONNECT") -> ElmFailure.UNABLE_TO_CONNECT
            text.startsWith(BUS_INIT) && text.contains(ERROR) -> ElmFailure.BUS_INIT_FAILED
            text.contains("CAN ERROR") -> ElmFailure.CAN_ERROR
            text.contains("DATA ERROR") -> ElmFailure.DATA_ERROR
            text.contains("BUS ERROR") -> ElmFailure.BUS_ERROR
            text.contains("BUFFER FULL") -> ElmFailure.BUFFER_FULL
            text.contains("STOPPED") -> ElmFailure.STOPPED
            text.contains("LV RESET") -> ElmFailure.LOW_POWER
            text.contains(ERROR) -> ElmFailure.GENERIC_ERROR
            else -> null
        }
    }

    private fun String.isTransientStatus(): Boolean {
        val text = uppercase()
        return text.startsWith(SEARCHING) || text.startsWith(BUS_INIT)
    }

    private fun String.matches(prefix: String): Boolean = uppercase().startsWith(prefix)

    private fun String.asDataOrText(): ElmResponse {
        val text = uppercase()
        val compact = normalized()
        return when {
            text == NO_DATA -> ElmResponse.NoData
            text == OK -> ElmResponse.Ok
            !compact.isHexDigits() -> ElmResponse.Text(this)
            compact.length % 2 != 0 -> ElmResponse.Unhandled(this)
            else -> ElmResponse.Data(compact.chunked(2).map { it.toInt(HEX_RADIX) })
        }
    }

    private fun String.isHexDigits(): Boolean =
        isNotEmpty() && all { it in '0'..'9' || it in 'A'..'F' }

    private fun String.normalized(): String = filterNot { it.isWhitespace() }.uppercase()

    private const val OK = "OK"
    private const val NO_DATA = "NO DATA"
    private const val SEARCHING = "SEARCHING"
    private const val BUS_INIT = "BUS INIT"
    private const val ERROR = "ERROR"
    private const val UNKNOWN = "?"
}
