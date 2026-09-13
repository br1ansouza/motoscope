package dev.br1ansouza.motoscope.core.telemetry

import kotlinx.coroutines.flow.Flow

interface TelemetryFeed {
    val isSimulated: Boolean

    fun events(): Flow<TelemetryEvent>
}
