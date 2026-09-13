package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock

internal class SessionJournal(
    private val store: RecordingStore,
    private val clock: MonotonicClock,
    private val wallClock: () -> Long
) {
    private var gapOpen = false

    suspend fun write(id: SessionId, type: SessionEventType, detail: String? = null) {
        store.appendEvent(
            id,
            SessionEvent(
                type = type,
                monotonicMillis = clock.millis(),
                wallClockEpochMillis = wallClock(),
                detail = detail
            )
        )
    }

    suspend fun openGapIfSilent(
        id: SessionId,
        lastSampleMonotonicMillis: Long?,
        absentAfterMillis: Long
    ) {
        val last = lastSampleMonotonicMillis ?: return
        if (gapOpen || clock.millis() - last < absentAfterMillis) return
        write(id, SessionEventType.DATA_GAP_STARTED)
        gapOpen = true
    }

    suspend fun closeGap(id: SessionId) {
        if (!gapOpen) return
        write(id, SessionEventType.DATA_GAP_ENDED)
        gapOpen = false
    }

    fun reset() {
        gapOpen = false
    }
}
