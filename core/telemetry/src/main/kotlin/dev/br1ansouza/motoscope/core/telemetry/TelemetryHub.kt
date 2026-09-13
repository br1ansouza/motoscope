package dev.br1ansouza.motoscope.core.telemetry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

class TelemetryHub(private val source: TelemetryFeed) : TelemetryFeed {
    private val stream = MutableSharedFlow<TelemetryEvent>(extraBufferCapacity = BUFFER)

    override val isSimulated: Boolean = source.isSimulated

    override fun events(): Flow<TelemetryEvent> = stream.asSharedFlow()

    suspend fun run() {
        stream.subscriptionCount.first { it > 0 }
        source.events().collect { stream.emit(it) }
    }

    private companion object {
        const val BUFFER = 256
    }
}
