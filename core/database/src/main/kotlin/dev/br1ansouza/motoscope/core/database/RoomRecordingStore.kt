package dev.br1ansouza.motoscope.core.database

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.recording.RecordingStore
import javax.inject.Inject

internal class RoomRecordingStore @Inject constructor(
    private val sessions: SessionDao,
    private val samples: SampleDao,
    private val events: SessionEventDao
) : RecordingStore {
    override suspend fun createSession(session: RecordingSession) {
        sessions.insert(
            SessionEntity(
                id = session.id.value,
                startedAtEpochMillis = session.startedAtEpochMillis,
                endedAtEpochMillis = session.endedAtEpochMillis,
                status = session.status.name
            )
        )
    }

    override suspend fun appendSamples(id: SessionId, samples: List<TelemetrySample>) {
        if (samples.isEmpty()) return
        this.samples.insertAll(samples.map { it.toEntity(id) })
    }

    override suspend fun appendEvent(id: SessionId, event: SessionEvent) {
        events.insert(
            SessionEventEntity(
                sessionId = id.value,
                type = event.type.name,
                detail = event.detail,
                monotonicMillis = event.monotonicMillis,
                recordedAtEpochMillis = event.wallClockEpochMillis
            )
        )
    }

    override suspend fun finishSession(
        id: SessionId,
        endedAtEpochMillis: Long,
        status: SessionStatus
    ): Boolean = sessions.finish(id.value, endedAtEpochMillis, status.name) == 1

    override suspend fun findUnfinished(): List<RecordingSession> =
        sessions.findUnfinished().map { entity ->
            RecordingSession(
                id = SessionId(entity.id),
                status = entity.status.toStatus(),
                startedAtEpochMillis = entity.startedAtEpochMillis,
                endedAtEpochMillis = entity.endedAtEpochMillis
            )
        }
}

private fun TelemetrySample.toEntity(id: SessionId) = SampleEntity(
    sessionId = id.value,
    metric = metric.name,
    value = value,
    unit = unit.name,
    source = source.name,
    monotonicMillis = monotonicMillis,
    recordedAtEpochMillis = wallClockEpochMillis
)

private fun String.toStatus(): SessionStatus =
    SessionStatus.entries.firstOrNull { it.name == this } ?: SessionStatus.INTERRUPTED
