package dev.br1ansouza.motoscope.core.settings

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import kotlinx.coroutines.flow.Flow

interface MetricVisibilityStore {
    fun visibleMetrics(): Flow<Set<TelemetryMetric>>

    suspend fun setVisible(metric: TelemetryMetric, visible: Boolean)
}
