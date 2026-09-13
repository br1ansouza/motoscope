package dev.br1ansouza.motoscope.protocol.elm327

sealed interface ElmCommand {
    val text: String

    data class At(override val text: String) : ElmCommand {
        init {
            require(text.startsWith("AT")) { "Comando AT precisa começar com AT, recebido $text." }
        }
    }

    data class ObdRequest(val mode: Int, val pid: Int?) : ElmCommand {
        init {
            require(mode in BYTE_MIN..BYTE_MAX) { "Modo fora de faixa: $mode." }
            require(pid == null || pid in BYTE_MIN..BYTE_MAX) { "PID fora de faixa: $pid." }
        }

        override val text: String
            get() = if (pid == null) {
                mode.toHexByte()
            } else {
                mode.toHexByte() + pid.toHexByte()
            }
    }

    companion object {
        val RESET = At("ATZ")
        val ECHO_OFF = At("ATE0")
        val LINEFEEDS_OFF = At("ATL0")
        val SPACES_OFF = At("ATS0")
        val HEADERS_OFF = At("ATH0")
        val HEADERS_ON = At("ATH1")
        val AUTO_PROTOCOL = At("ATSP0")
        val DESCRIBE_PROTOCOL = At("ATDP")
        val READ_ADAPTER_VOLTAGE = At("ATRV")
    }
}

internal fun Int.toHexByte(): String = toString(HEX_RADIX).uppercase().padStart(2, '0')
