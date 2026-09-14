package dev.br1ansouza.motoscope.core.telemetry

import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.Freshness
import dev.br1ansouza.motoscope.core.model.FreshnessWindow
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.runningFold

class TelemetryEngine(
    private val feed: TelemetryFeed,
    private val clock: MonotonicClock,
    private val window: FreshnessWindow,
    private val refreshIntervalMillis: Long
) {
    init {
        require(refreshIntervalMillis > 0) {
            "Intervalo de reavaliação precisa ser positivo, recebido $refreshIntervalMillis."
        }
    }

    fun state(): Flow<LiveTelemetry> {
        val inputs = merge(
            feed.events().map<TelemetryEvent, Input> { Input.Received(it) },
            refreshTicks()
        )
        return inputs
            .runningFold(Snapshot(LiveTelemetry(simulated = feed.isSimulated))) { snapshot, input ->
                snapshot.reduce(input)
            }
            .map { it.live }
            .distinctUntilChanged()
    }

    private fun refreshTicks(): Flow<Input> = flow {
        while (true) {
            delay(refreshIntervalMillis)
            emit(Input.Refresh)
        }
    }

    private fun Snapshot.reduce(input: Input): Snapshot = when (input) {
        is Input.Refresh -> refreshed()
        is Input.Received -> when (val event = input.event) {
            is TelemetryEvent.TransportChanged ->
                copy(live = live.copy(transport = event.state)).refreshed()

            is TelemetryEvent.EcuChanged ->
                copy(reportedEcu = event.state).refreshed()

            is TelemetryEvent.SampleReceived -> {
                val reading = MetricReading(event.sample, Freshness.FRESH)
                val readings = live.readings + (event.sample.metric to reading)
                copy(live = live.copy(readings = readings)).refreshed()
            }
        }
    }

    private fun Snapshot.refreshed(): Snapshot {
        val now = clock.millis()
        val readings = live.readings.reclassified(now)
        val ecu = ecuFrom(readings)
        if (readings === live.readings && ecu == live.ecu) return this
        return copy(live = live.copy(readings = readings, ecu = ecu))
    }

    private fun Map<TelemetryMetric, MetricReading>.reclassified(
        nowMillis: Long
    ): Map<TelemetryMetric, MetricReading> {
        var updated: MutableMap<TelemetryMetric, MetricReading>? = null
        for ((metric, reading) in this) {
            val freshness = window.classify(reading.sample, nowMillis)
            if (freshness == reading.freshness) continue
            val target = updated ?: LinkedHashMap(this).also { updated = it }
            target[metric] = reading.copy(freshness = freshness)
        }
        return updated ?: this
    }

    private fun Snapshot.ecuFrom(readings: Map<TelemetryMetric, MetricReading>): EcuState {
        val states = readings.values.map { it.freshness }
        return when {
            states.isEmpty() -> reportedEcu
            states.all { it == Freshness.ABSENT } -> EcuState.NOT_RESPONDING
            states.any { it == Freshness.FRESH } -> EcuState.RESPONDING
            else -> EcuState.DELAYED
        }
    }

    private data class Snapshot(
        val live: LiveTelemetry,
        val reportedEcu: EcuState = EcuState.UNKNOWN
    )

    private sealed interface Input {
        data object Refresh : Input

        data class Received(val event: TelemetryEvent) : Input
    }
}
