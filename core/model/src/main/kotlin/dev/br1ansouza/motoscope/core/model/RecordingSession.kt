package dev.br1ansouza.motoscope.core.model

data class RecordingSession(
    val id: SessionId,
    val status: SessionStatus,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null
) {
    init {
        require(startedAtEpochMillis >= 0) {
            "Início da sessão não pode ser negativo, recebido $startedAtEpochMillis."
        }
        require(endedAtEpochMillis == null || endedAtEpochMillis >= startedAtEpochMillis) {
            "Fim da sessão não pode anteceder o início."
        }
        require((status == SessionStatus.RECORDING) == (endedAtEpochMillis == null)) {
            "Sessão em $status é incompatível com fim $endedAtEpochMillis."
        }
    }
}
