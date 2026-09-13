package dev.br1ansouza.motoscope.core.telemetry

import dev.br1ansouza.motoscope.core.model.TransportState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryHubTest {
    @Test
    fun oneSourceServesUiAndRecorderAndStopsAfterLastSubscriber() = runTest {
        var connections = 0
        var disconnections = 0
        val feed = object : TelemetryFeed {
            override val isSimulated = true
            override fun events() = flow {
                connections++
                try {
                    emit(TelemetryEvent.TransportChanged(TransportState.CONNECTED))
                    awaitCancellation()
                } finally {
                    disconnections++
                }
            }
        }
        val hub = TelemetryHub(feed)
        backgroundScope.launch { hub.run() }
        runCurrent()
        assertEquals(0, connections)
        val ui = backgroundScope.launch { hub.events().collect() }
        runCurrent()
        val recorder = backgroundScope.launch { hub.events().collect() }
        runCurrent()
        assertEquals(1, connections)
        assertEquals(
            TelemetryEvent.TransportChanged(TransportState.CONNECTED),
            hub.events().first()
        )
        ui.cancel()
        runCurrent()
        assertEquals(0, disconnections)
        recorder.cancel()
        runCurrent()
        assertEquals(1, disconnections)
        backgroundScope.launch { hub.events().collect() }
        runCurrent()
        assertEquals(2, connections)
    }
}
