package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TransportState
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RecordingEngine(
    private val events: Flow<TelemetryEvent>,
    private val store: RecordingStore,
    private val clock: MonotonicClock,
    private val wallClock: () -> Long,
    private val newSessionId: () -> SessionId,
    private val tuning: RecordingTuning
) {
    private val mutex = Mutex()
    private val pending = mutableListOf<TelemetrySample>()
    private val mutableState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    private val journal = SessionJournal(store, clock, wallClock)

    private var session: RecordingSession? = null
    private var samplesWritten = 0L
    private var lastSampleMonotonicMillis: Long? = null
    private var transportLost = false

    val state: StateFlow<RecordingState> = mutableState.asStateFlow()

    suspend fun start(): RecordingSession = mutex.withLock {
        session ?: openSession()
    }

    suspend fun stop(): Boolean = mutex.withLock {
        val current = session ?: return@withLock false
        flushPending(current.id)
        journal.write(current.id, SessionEventType.RECORDING_FINISHED)
        store.finishSession(current.id, wallClock(), SessionStatus.FINISHED)
        resetSession()
        true
    }

    suspend fun run() {
        val inputs = merge(
            events.map<TelemetryEvent, Input> { Input.Received(it) },
            flushTicks(tuning.flushIntervalMillis)
        )
        inputs.collect { input -> mutex.withLock { handle(input) } }
    }

    private suspend fun openSession(): RecordingSession {
        val opened = RecordingSession(
            id = newSessionId(),
            status = SessionStatus.RECORDING,
            startedAtEpochMillis = wallClock()
        )
        store.createSession(opened)
        session = opened
        samplesWritten = 0
        lastSampleMonotonicMillis = null
        pending.clear()
        journal.reset()
        journal.write(opened.id, SessionEventType.RECORDING_STARTED)
        publish()
        return opened
    }

    private fun resetSession() {
        session = null
        samplesWritten = 0
        lastSampleMonotonicMillis = null
        pending.clear()
        journal.reset()
        publish()
    }

    private suspend fun handle(input: Input) {
        val current = session
        when (input) {
            is Input.Flush -> if (current != null) {
                journal.openGapIfSilent(
                    id = current.id,
                    lastSampleMonotonicMillis = lastSampleMonotonicMillis,
                    absentAfterMillis = tuning.window.absentAfterMillis
                )
                flushPending(current.id)
            }

            is Input.Received -> when (val event = input.event) {
                is TelemetryEvent.SampleReceived -> onSample(current, event.sample)
                is TelemetryEvent.TransportChanged -> onTransport(current, event.state)
                is TelemetryEvent.EcuChanged -> Unit
            }
        }
        publish()
    }

    private suspend fun onSample(current: RecordingSession?, sample: TelemetrySample) {
        lastSampleMonotonicMillis = sample.monotonicMillis
        if (current == null) return
        journal.closeGap(current.id)
        pending += sample
        if (pending.size >= tuning.batchSize) flushPending(current.id)
    }

    private suspend fun onTransport(current: RecordingSession?, state: TransportState) {
        val lost = state != TransportState.CONNECTED
        if (lost == transportLost) return
        transportLost = lost
        if (current == null) return
        val type = if (lost) {
            SessionEventType.TRANSPORT_LOST
        } else {
            SessionEventType.TRANSPORT_RECOVERED
        }
        journal.write(current.id, type, state.name)
    }

    private suspend fun flushPending(id: SessionId) {
        if (pending.isEmpty()) return
        val batch = pending.toList()
        store.appendSamples(id, batch)
        samplesWritten += batch.size
        pending.clear()
    }

    private fun publish() {
        val current = session
        mutableState.value = if (current == null) {
            RecordingState.Idle
        } else {
            RecordingState.Active(current, samplesWritten, pending.size)
        }
    }
}

private sealed interface Input {
    data object Flush : Input

    data class Received(val event: TelemetryEvent) : Input
}

private fun flushTicks(intervalMillis: Long): Flow<Input> = flow {
    while (true) {
        delay(intervalMillis)
        emit(Input.Flush)
    }
}
