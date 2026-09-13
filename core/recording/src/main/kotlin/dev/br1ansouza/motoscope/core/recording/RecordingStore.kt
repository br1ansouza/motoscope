package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetrySample

interface RecordingStore {
    suspend fun createSession(session: RecordingSession, event: SessionEvent)

    suspend fun appendSamples(id: SessionId, samples: List<TelemetrySample>)

    suspend fun appendEvent(id: SessionId, event: SessionEvent)

    suspend fun completeSession(
        id: SessionId,
        samples: List<TelemetrySample>,
        event: SessionEvent,
        status: SessionStatus
    )

    suspend fun resumeSession(id: SessionId, event: SessionEvent): Long

    suspend fun markUnfinishedInterrupted()

    suspend fun findUnfinished(): List<RecordingSession>
}
