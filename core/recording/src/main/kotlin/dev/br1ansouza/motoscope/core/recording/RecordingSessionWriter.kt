package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TransportState
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEvent

internal class RecordingSessionWriter(
    private val store: RecordingStore,
    private val clock: MonotonicClock,
    private val wallClock: () -> Long,
    private val newSessionId: () -> SessionId,
    private val tuning: RecordingTuning
) {
    private val pending = mutableListOf<TelemetrySample>()
    private val journal = SessionJournal(store, clock, wallClock)
    private var prepared = false
    private var session: RecordingSession? = null
    private var samplesWritten = 0L
    private var lastSampleMonotonicMillis: Long? = null
    private var transportLost = false

    suspend fun prepare() {
        if (prepared) return
        store.markUnfinishedInterrupted()
        prepared = true
    }

    suspend fun open(): RecordingSession {
        prepare()
        session?.let { return it }
        val opened = RecordingSession(newSessionId(), SessionStatus.RECORDING, wallClock())
        store.createSession(opened, event(SessionEventType.RECORDING_STARTED))
        session = opened
        lastSampleMonotonicMillis = clock.millis()
        return opened
    }

    suspend fun restore(): Boolean {
        prepare()
        if (session != null) return true
        val previous = store.findUnfinished().lastOrNull()
        if (previous != null) {
            samplesWritten = store.resumeSession(previous.id, event(SessionEventType.RECORDING_RESUMED))
            session = previous.copy(status = SessionStatus.RECORDING)
            lastSampleMonotonicMillis = clock.millis()
        }
        return previous != null
    }

    suspend fun finish(status: SessionStatus, type: SessionEventType): Boolean {
        val current = session ?: return false
        val finalEvent = event(type).copy(
            wallClockEpochMillis = wallClock().coerceAtLeast(current.startedAtEpochMillis)
        )
        store.completeSession(current.id, pending.toList(), finalEvent, status)
        session = null
        samplesWritten = 0
        lastSampleMonotonicMillis = null
        transportLost = false
        pending.clear()
        journal.reset()
        return true
    }

    suspend fun handle(input: RecordingInput) {
        val current = session ?: return
        when (input) {
            RecordingInput.Flush -> {
                journal.openGapIfSilent(
                    current.id,
                    lastSampleMonotonicMillis,
                    tuning.window.absentAfterMillis
                )
                flushPending(current.id)
            }
            is RecordingInput.Received -> when (val event = input.event) {
                is TelemetryEvent.SampleReceived -> onSample(current, event.sample)
                is TelemetryEvent.TransportChanged -> onTransport(current, event.state)
                is TelemetryEvent.EcuChanged -> Unit
            }
        }
    }

    private suspend fun onSample(current: RecordingSession, sample: TelemetrySample) {
        lastSampleMonotonicMillis = sample.monotonicMillis
        journal.closeGap(current.id)
        pending += sample
        if (pending.size >= tuning.batchSize) flushPending(current.id)
    }

    private suspend fun onTransport(current: RecordingSession, state: TransportState) {
        val lost = state != TransportState.CONNECTED
        if (lost == transportLost) return
        val type = if (lost) {
            SessionEventType.TRANSPORT_LOST
        } else {
            SessionEventType.TRANSPORT_RECOVERED
        }
        journal.write(current.id, type, state.name)
        transportLost = lost
    }

    private suspend fun flushPending(id: SessionId) {
        if (pending.isEmpty()) return
        val batch = pending.toList()
        store.appendSamples(id, batch)
        samplesWritten += batch.size
        pending.clear()
    }

    private fun event(type: SessionEventType) = SessionEvent(type, clock.millis(), wallClock())

    fun state(): RecordingState = session?.let {
        RecordingState.Active(it, samplesWritten, pending.size)
    } ?: RecordingState.Idle
}
