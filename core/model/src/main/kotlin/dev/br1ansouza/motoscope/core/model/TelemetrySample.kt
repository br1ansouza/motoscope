package dev.br1ansouza.motoscope.core.model

data class TelemetrySample(
    val metric: TelemetryMetric,
    val value: Double,
    val unit: MetricUnit,
    val source: TelemetrySource,
    val monotonicMillis: Long,
    val wallClockEpochMillis: Long
) {
    init {
        require(value.isFinite()) { "Valor de $metric precisa ser finito, recebido $value." }
        require(monotonicMillis >= 0) {
            "Instante monotônico de $metric não pode ser negativo, recebido $monotonicMillis."
        }
    }

    fun ageMillisAt(nowMonotonicMillis: Long): Long {
        require(nowMonotonicMillis >= monotonicMillis) {
            "Instante atual $nowMonotonicMillis é anterior à amostra $monotonicMillis."
        }
        return nowMonotonicMillis - monotonicMillis
    }
}
