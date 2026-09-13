package dev.br1ansouza.motoscope.core.database

import androidx.room.withTransaction
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.recording.RecordingStore
import javax.inject.Inject

internal class RoomRecordingStore @Inject constructor(
    private val database: MotoScopeDatabase,
    private val sessions: SessionDao,
    private val samples: SampleDao,
    private val events: SessionEventDao
) : RecordingStore {
    override suspend fun createSession(session: RecordingSession, event: SessionEvent) {
        database.withTransaction {
            sessions.insert(
                SessionEntity(
                    id = session.id.value,
                    startedAtEpochMillis = session.startedAtEpochMillis,
                    endedAtEpochMillis = session.endedAtEpochMillis,
                    status = session.status.name
                )
            )
            appendEvent(session.id, event)
        }
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

    override suspend fun completeSession(
        id: SessionId,
        samples: List<TelemetrySample>,
        event: SessionEvent,
        status: SessionStatus
    ) {
        database.withTransaction {
            check(sessions.finish(id.value, event.wallClockEpochMillis, status.name) == 1) {
                "Não foi possível encerrar a sessão."
            }
            appendSamples(id, samples)
            appendEvent(id, event)
        }
    }

    override suspend fun resumeSession(id: SessionId, event: SessionEvent): Long =
        database.withTransaction {
            check(sessions.resume(id.value) == 1) { "Não foi possível recuperar a sessão." }
            appendEvent(id, event)
            samples.countBySession(id.value).toLong()
        }

    override suspend fun markUnfinishedInterrupted() = sessions.markUnfinishedInterrupted()

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
