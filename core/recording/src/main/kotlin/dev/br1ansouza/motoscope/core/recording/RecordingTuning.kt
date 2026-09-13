package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.FreshnessWindow

data class RecordingTuning(
    val batchSize: Int,
    val flushIntervalMillis: Long,
    val window: FreshnessWindow
) {
    init {
        require(batchSize >= 1) {
            "Tamanho do lote precisa ser ao menos 1, recebido $batchSize."
        }
        require(flushIntervalMillis > 0) {
            "Intervalo de descarga precisa ser positivo, recebido $flushIntervalMillis."
        }
    }
}
