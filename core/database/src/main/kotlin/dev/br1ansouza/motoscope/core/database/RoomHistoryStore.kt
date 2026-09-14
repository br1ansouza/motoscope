package dev.br1ansouza.motoscope.core.database

import dev.br1ansouza.motoscope.core.history.HistoryStore
import dev.br1ansouza.motoscope.core.history.StoredSample
import dev.br1ansouza.motoscope.core.model.MetricSummary
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.SessionSummary
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TelemetrySource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomHistoryStore @Inject constructor(
    private val sessions: SessionDao,
    private val samples: SampleDao,
    private val events: SessionEventDao
) : HistoryStore {
    override fun sessions(): Flow<List<RecordingSession>> =
        sessions.observeSessions().map { entities -> entities.mapNotNull { it.toSession() } }

    override suspend fun summary(id: SessionId): SessionSummary? {
        val session = sessions.find(id.value)?.toSession() ?: return null
        val metrics = samples.summarize(id.value).mapNotNull { it.toSummary() }
        return SessionSummary(
            session = session,
            sampleCount = metrics.sumOf { it.count },
            metrics = metrics
        )
    }

    override suspend fun events(id: SessionId): List<SessionEvent> =
        events.findBySession(id.value).mapNotNull { entity ->
            val type = SessionEventType.entries.firstOrNull { it.name == entity.type }
                ?: return@mapNotNull null
            SessionEvent(
                type = type,
                monotonicMillis = entity.monotonicMillis,
                wallClockEpochMillis = entity.recordedAtEpochMillis,
                detail = entity.detail
            )
        }

    override suspend fun samplesAfter(id: SessionId, rowId: Long, limit: Int): List<StoredSample> {
        require(limit > 0) { "Tamanho de página precisa ser positivo, recebido $limit." }
        return samples.page(id.value, rowId, limit).mapNotNull { entity ->
            val sample = entity.toSample() ?: return@mapNotNull null
            StoredSample(rowId = entity.id, sample = sample)
        }
    }

    override suspend fun delete(id: SessionId) {
        sessions.delete(id.value)
    }
}

private fun SessionEntity.toSession(): RecordingSession? =
    SessionStatus.entries.firstOrNull { it.name == status }
        ?.takeIf { consistentWith(it) }
        ?.let { parsed ->
            RecordingSession(
                id = SessionId(id),
                status = parsed,
                startedAtEpochMillis = startedAtEpochMillis,
                endedAtEpochMillis = endedAtEpochMillis
            )
        }

private fun SessionEntity.consistentWith(parsed: SessionStatus): Boolean {
    val ended = endedAtEpochMillis
    if (ended != null && ended < startedAtEpochMillis) return false
    return when (parsed) {
        SessionStatus.RECORDING -> ended == null
        SessionStatus.FINISHED -> ended != null
        SessionStatus.INTERRUPTED -> true
    }
}

private fun MetricSummaryRow.toSummary(): MetricSummary? {
    val telemetryMetric = TelemetryMetric.entries.firstOrNull { it.name == metric }
    val metricUnit = MetricUnit.entries.firstOrNull { it.name == unit }
    val usable = telemetryMetric != null && metricUnit != null &&
        count > 0 && maximum >= minimum
    return if (!usable) {
        null
    } else {
        MetricSummary(
            metric = telemetryMetric,
            unit = metricUnit,
            count = count,
            minimum = minimum,
            maximum = maximum,
            average = average
        )
    }
}

private fun SampleEntity.toSample(): TelemetrySample? {
    val telemetryMetric = TelemetryMetric.entries.firstOrNull { it.name == metric }
    val metricUnit = MetricUnit.entries.firstOrNull { it.name == unit }
    val telemetrySource = TelemetrySource.entries.firstOrNull { it.name == source }
    return if (telemetryMetric == null || metricUnit == null || telemetrySource == null) {
        null
    } else {
        TelemetrySample(
            metric = telemetryMetric,
            value = value,
            unit = metricUnit,
            source = telemetrySource,
            monotonicMillis = monotonicMillis,
            wallClockEpochMillis = recordedAtEpochMillis
        )
    }
}
