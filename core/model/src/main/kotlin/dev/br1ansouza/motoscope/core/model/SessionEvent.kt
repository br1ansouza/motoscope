package dev.br1ansouza.motoscope.core.model

data class SessionEvent(
    val type: SessionEventType,
    val monotonicMillis: Long,
    val wallClockEpochMillis: Long,
    val detail: String? = null
) {
    init {
        require(monotonicMillis >= 0) {
            "Instante monotônico não pode ser negativo, recebido $monotonicMillis."
        }
        require(wallClockEpochMillis >= 0) {
            "Horário civil não pode ser negativo, recebido $wallClockEpochMillis."
        }
    }
}
