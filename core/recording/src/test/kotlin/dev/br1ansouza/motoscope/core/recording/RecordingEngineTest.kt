package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.FreshnessWindow
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TelemetrySource
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingEngineTest {
    private val store = TestRecordingStore()
    private val events = MutableSharedFlow<TelemetryEvent>()
    private var civilTime = 100L

    private fun TestScope.engine() = RecordingEngine(
        events,
        store,
        MonotonicClock { testScheduler.currentTime },
        { civilTime },
        { SessionId("session-${store.sessions.size}") },
        RecordingTuning(128, 2000, FreshnessWindow(1500, 4000))
    )

    @Test
    fun collectionRunsOnlyWhileRecordingAndStopFlushesHiddenMetrics() = runTest {
        val engine = engine()
        backgroundScope.launch { engine.run() }
        runCurrent()
        assertEquals(0, events.subscriptionCount.value)
        engine.start()
        runCurrent()
        assertEquals(1, events.subscriptionCount.value)
        events.emit(TelemetryEvent.SampleReceived(sample()))
        runCurrent()
        assertEquals(0, store.samples.size)
        engine.stop()
        runCurrent()
        assertEquals(1, store.samples.size)
        assertEquals(SessionStatus.FINISHED, store.sessions.single().status)
        assertEquals(0, events.subscriptionCount.value)
        assertFalse(engine.stop())
    }

    @Test
    fun cancellationDuringFinalWriteDoesNotLosePendingSamples() = runTest {
        val engine = engine()
        backgroundScope.launch { engine.run() }
        engine.start()
        runCurrent()
        events.emit(TelemetryEvent.SampleReceived(sample()))
        runCurrent()
        val gate = CompletableDeferred<Unit>()
        store.completionGate = gate
        val stop = launch { engine.stop() }
        runCurrent()
        stop.cancel()
        gate.complete(Unit)
        stop.join()
        assertEquals(1, store.samples.size)
        assertEquals(RecordingState.Idle, engine.state.value)
    }

    @Test
    fun failedFlushStopsCollectionAndRetainsBatchForExplicitRetry() = runTest {
        val engine = engine()
        backgroundScope.launch { engine.run() }
        engine.start()
        runCurrent()
        events.emit(TelemetryEvent.SampleReceived(sample()))
        runCurrent()
        store.failAppend = true
        advanceTimeBy(2000)
        runCurrent()
        assertEquals(RecordingState.Failed, engine.state.value)
        assertEquals(0, events.subscriptionCount.value)
        engine.stop()
        assertEquals(1, store.samples.size)
        assertEquals(RecordingState.Idle, engine.state.value)
    }

    @Test
    fun restoredProcessUsesSameSessionAndPersistedCountWithoutDuplicatingStart() = runTest {
        val original = engine()
        original.start()
        store.samples += sample()
        val restored = engine()
        restored.prepare()
        assertEquals(SessionStatus.INTERRUPTED, store.sessions.single().status)
        assertTrue(restored.restore())
        assertTrue(restored.restore())
        assertEquals(1, store.sessions.size)
        assertEquals(1L, (restored.state.value as RecordingState.Active).samplesWritten)
        assertEquals(1, store.events.count { it.type == SessionEventType.RECORDING_RESUMED })
        civilTime = 50
        restored.stop()
        assertEquals(100L, store.sessions.single().endedAtEpochMillis)
    }

    @Test
    fun silenceFromStartCreatesGapAndEmptyRestoreCreatesNoSession() = runTest {
        val engine = engine()
        assertFalse(engine.restore())
        backgroundScope.launch { engine.run() }
        engine.start()
        runCurrent()
        advanceTimeBy(4000)
        runCurrent()
        assertEquals(1, store.events.count { it.type == SessionEventType.DATA_GAP_STARTED })
        events.emit(TelemetryEvent.SampleReceived(sample()))
        runCurrent()
        assertEquals(1, store.events.count { it.type == SessionEventType.DATA_GAP_ENDED })
    }

    private fun sample() = TelemetrySample(
        TelemetryMetric.ENGINE_RPM,
        1200.0,
        MetricUnit.REVOLUTIONS_PER_MINUTE,
        TelemetrySource.SIMULATOR,
        0,
        100
    )
}

private class TestRecordingStore : RecordingStore {
    val sessions = mutableListOf<RecordingSession>()
    val samples = mutableListOf<TelemetrySample>()
    val events = mutableListOf<SessionEvent>()
    var completionGate: CompletableDeferred<Unit>? = null
    var failAppend = false

    override suspend fun createSession(session: RecordingSession, event: SessionEvent) {
        sessions += session
        events += event
    }

    override suspend fun appendSamples(id: SessionId, samples: List<TelemetrySample>) {
        check(!failAppend)
        this.samples += samples
    }

    override suspend fun appendEvent(id: SessionId, event: SessionEvent) {
        events += event
    }

    override suspend fun completeSession(
        id: SessionId,
        samples: List<TelemetrySample>,
        event: SessionEvent,
        status: SessionStatus
    ) {
        completionGate?.await()
        val index = sessions.indexOfFirst { it.id == id }
        check(index >= 0 && sessions[index].endedAtEpochMillis == null)
        this.samples += samples
        events += event
        sessions[index] =
            sessions[index].copy(status = status, endedAtEpochMillis = event.wallClockEpochMillis)
    }

    override suspend fun resumeSession(id: SessionId, event: SessionEvent): Long {
        val index = sessions.indexOfFirst { it.id == id }
        sessions[index] = sessions[index].copy(status = SessionStatus.RECORDING)
        events += event
        return samples.size.toLong()
    }

    override suspend fun markUnfinishedInterrupted() {
        sessions.indices.filter { sessions[it].endedAtEpochMillis == null }.forEach {
            sessions[it] = sessions[it].copy(status = SessionStatus.INTERRUPTED)
        }
    }

    override suspend fun findUnfinished() = sessions.filter { it.endedAtEpochMillis == null }
}
