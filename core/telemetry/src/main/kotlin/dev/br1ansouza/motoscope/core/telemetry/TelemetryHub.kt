package dev.br1ansouza.motoscope.core.telemetry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription

class TelemetryHub(private val source: TelemetryFeed) : TelemetryFeed {
    private val stream = MutableSharedFlow<TelemetryEvent>(extraBufferCapacity = BUFFER)

    @Volatile
    private var transport: TelemetryEvent.TransportChanged? = null

    @Volatile
    private var ecu: TelemetryEvent.EcuChanged? = null

    override val isSimulated: Boolean = source.isSimulated

    override fun events(): Flow<TelemetryEvent> = stream.onSubscription {
        transport?.let { emit(it) }
        ecu?.let { emit(it) }
    }

    suspend fun run() {
        stream.subscriptionCount.map { it > 0 }.distinctUntilChanged().collectLatest { active ->
            if (active) {
                try {
                    source.events().collect { event ->
                        when (event) {
                            is TelemetryEvent.TransportChanged -> transport = event
                            is TelemetryEvent.EcuChanged -> ecu = event
                            is TelemetryEvent.SampleReceived -> Unit
                        }
                        stream.emit(event)
                    }
                } finally {
                    transport = null
                    ecu = null
                }
            }
        }
    }

    private companion object {
        const val BUFFER = 256
    }
}
