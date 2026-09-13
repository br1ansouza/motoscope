package dev.br1ansouza.motoscope.core.model

data class FreshnessWindow(val delayedAfterMillis: Long, val absentAfterMillis: Long) {
    init {
        require(delayedAfterMillis > 0) {
            "Limite de atraso precisa ser positivo, recebido $delayedAfterMillis."
        }
        require(absentAfterMillis > delayedAfterMillis) {
            "Limite de ausência $absentAfterMillis precisa superar o de atraso $delayedAfterMillis."
        }
    }

    fun classify(ageMillis: Long): Freshness {
        require(ageMillis >= 0) { "Idade não pode ser negativa, recebida $ageMillis." }
        return when {
            ageMillis >= absentAfterMillis -> Freshness.ABSENT
            ageMillis >= delayedAfterMillis -> Freshness.DELAYED
            else -> Freshness.FRESH
        }
    }

    fun classify(sample: TelemetrySample, nowMonotonicMillis: Long): Freshness =
        classify(sample.ageMillisAt(nowMonotonicMillis))
}
