package dev.br1ansouza.motoscope.protocol.elm327

sealed interface ElmCommand {
    val text: String

    data class At(override val text: String) : ElmCommand {
        init {
            require(text in ALLOWED_ADAPTER_COMMANDS) { "Comando AT não autorizado pelo MVP." }
        }
    }

    data class ObdRequest(val mode: Int, val pid: Int?) : ElmCommand {
        init {
            require(mode in READ_ONLY_MODES) { "O MVP permite somente modos OBD de leitura." }
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

private val ALLOWED_ADAPTER_COMMANDS = setOf(
    "ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATH1", "ATSP0", "ATDP", "ATRV"
)
private const val CURRENT_DATA = 0x01
private const val FREEZE_FRAME = 0x02
private const val STORED_DTC = 0x03
private const val MONITOR_RESULTS = 0x06
private const val PENDING_DTC = 0x07
private const val VEHICLE_INFORMATION = 0x09
private const val PERMANENT_DTC = 0x0A
private val READ_ONLY_MODES = setOf(
    CURRENT_DATA,
    FREEZE_FRAME,
    STORED_DTC,
    MONITOR_RESULTS,
    PENDING_DTC,
    VEHICLE_INFORMATION,
    PERMANENT_DTC
)
