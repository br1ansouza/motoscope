package dev.br1ansouza.motoscope.core.model

data class MetricSummary(
    val metric: TelemetryMetric,
    val unit: MetricUnit,
    val count: Long,
    val minimum: Double,
    val maximum: Double,
    val average: Double
) {
    init {
        require(count > 0) {
            "Resumo de $metric precisa de ao menos uma amostra, recebido $count."
        }
        require(maximum >= minimum) {
            "Máximo $maximum de $metric não pode ser menor que o mínimo $minimum."
        }
    }
}

data class SessionSummary(
    val session: RecordingSession,
    val sampleCount: Long,
    val metrics: List<MetricSummary>
) {
    init {
        require(sampleCount >= 0) {
            "Contagem de amostras não pode ser negativa, recebida $sampleCount."
        }
        require(metrics.distinctBy { it.metric }.size == metrics.size) {
            "Resumo não pode repetir métrica."
        }
    }

    val durationMillis: Long?
        get() = session.endedAtEpochMillis?.minus(session.startedAtEpochMillis)
}
