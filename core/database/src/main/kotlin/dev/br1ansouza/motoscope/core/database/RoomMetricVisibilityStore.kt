package dev.br1ansouza.motoscope.core.database

import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.settings.DashboardMetrics
import dev.br1ansouza.motoscope.core.settings.MetricVisibilityStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomMetricVisibilityStore @Inject constructor(private val dao: MetricPreferenceDao) :
    MetricVisibilityStore {
    override fun visibleMetrics(): Flow<Set<TelemetryMetric>> = dao.observeAll().map { stored ->
        val known = stored.mapNotNull { row -> row.toMetric()?.let { it to row.visible } }
        val overrides = known.toMap()
        DashboardMetrics.SELECTABLE
            .filter { overrides[it] ?: (it in DashboardMetrics.DEFAULT_VISIBLE) }
            .toSet()
    }

    override suspend fun setVisible(metric: TelemetryMetric, visible: Boolean) {
        dao.upsert(MetricPreferenceEntity(metric.name, visible))
    }
}

private fun MetricPreferenceEntity.toMetric(): TelemetryMetric? =
    TelemetryMetric.entries.firstOrNull { it.name == metric }
