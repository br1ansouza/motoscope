package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RecordingEngine(
    private val events: Flow<TelemetryEvent>,
    store: RecordingStore,
    clock: MonotonicClock,
    wallClock: () -> Long,
    newSessionId: () -> SessionId,
    private val tuning: RecordingTuning
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    private val writer = RecordingSessionWriter(store, clock, wallClock, newSessionId, tuning)

    val state: StateFlow<RecordingState> = mutableState.asStateFlow()

    suspend fun start(): RecordingSession = withContext(NonCancellable) {
        mutex.withLock {
            writer.open().also { mutableState.value = writer.state() }
        }
    }

    suspend fun restore(): Boolean = withContext(NonCancellable) {
        mutex.withLock {
            writer.restore().also { mutableState.value = writer.state() }
        }
    }

    suspend fun prepare() = mutex.withLock { writer.prepare() }

    suspend fun stop(): Boolean =
        finish(SessionStatus.FINISHED, SessionEventType.RECORDING_FINISHED)

    suspend fun interrupt(): Boolean =
        finish(SessionStatus.INTERRUPTED, SessionEventType.RECORDING_INTERRUPTED)

    private suspend fun finish(status: SessionStatus, type: SessionEventType): Boolean =
        withContext(NonCancellable) {
            mutex.withLock {
                writer.finish(status, type).also { mutableState.value = writer.state() }
            }
        }

    suspend fun reportFailure() = mutex.withLock {
        mutableState.value = RecordingState.Failed
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun run() {
        val inputs = merge(
            events.map<TelemetryEvent, RecordingInput> { RecordingInput.Received(it) },
            flushTicks(tuning.flushIntervalMillis)
        )
        state.map { it is RecordingState.Active }.distinctUntilChanged().collectLatest { active ->
            if (active) {
                try {
                    inputs.collect { input ->
                        withContext(NonCancellable) {
                            mutex.withLock {
                                if (mutableState.value is RecordingState.Active) {
                                    writer.handle(input)
                                    mutableState.value = writer.state()
                                }
                            }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    reportFailure()
                }
            }
        }
    }
}

internal sealed interface RecordingInput {
    data object Flush : RecordingInput
    data class Received(val event: TelemetryEvent) : RecordingInput
}

private fun flushTicks(intervalMillis: Long): Flow<RecordingInput> = flow {
    while (true) {
        delay(intervalMillis)
        emit(RecordingInput.Flush)
    }
}
