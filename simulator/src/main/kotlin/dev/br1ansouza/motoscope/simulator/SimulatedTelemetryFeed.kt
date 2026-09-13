package dev.br1ansouza.motoscope.simulator

import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TelemetrySource
import dev.br1ansouza.motoscope.core.model.TransportState
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEvent
import dev.br1ansouza.motoscope.core.telemetry.TelemetryFeed
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SimulatedTelemetryFeed(
    private val clock: MonotonicClock,
    private val wallClock: () -> Long = System::currentTimeMillis,
    private val sampleIntervalMillis: Long = FAST_INTERVAL_MILLIS
) : TelemetryFeed {
    init {
        require(sampleIntervalMillis > 0) { "Intervalo do simulador precisa ser positivo." }
    }

    override val isSimulated: Boolean = true

    override fun events(): Flow<TelemetryEvent> = flow {
        emit(TelemetryEvent.TransportChanged(TransportState.CONNECTING))
        delay(CONNECT_DELAY_MILLIS)
        emit(TelemetryEvent.TransportChanged(TransportState.CONNECTED))
        delay(ECU_DELAY_MILLIS)
        emit(TelemetryEvent.EcuChanged(EcuState.RESPONDING))

        val start = clock.millis()
        var tick = 0L
        var reconnecting = false
        while (true) {
            val elapsedSeconds = (clock.millis() - start) / MILLIS_IN_SECOND
            val silent = elapsedSeconds.inDropout()
            if (silent != reconnecting) {
                reconnecting = silent
                emit(TelemetryEvent.TransportChanged(transportFor(silent)))
            }
            if (!silent) {
                samplesAt(elapsedSeconds, tick).forEach { emit(TelemetryEvent.SampleReceived(it)) }
            }
            tick++
            delay(sampleIntervalMillis)
        }
    }

    private fun transportFor(silent: Boolean): TransportState =
        if (silent) TransportState.RECONNECTING else TransportState.CONNECTED

    private fun Double.inDropout(): Boolean {
        val position = this % DROPOUT_CYCLE_SECONDS
        return position >= DROPOUT_START_SECONDS && position < DROPOUT_END_SECONDS
    }

    private fun samplesAt(seconds: Double, tick: Long): List<TelemetrySample> {
        val throttle = throttleAt(seconds)
        val engineSpeed = IDLE_RPM + throttle * RPM_SPAN
        val fast = listOf(
            sample(TelemetryMetric.ENGINE_RPM, engineSpeed, MetricUnit.REVOLUTIONS_PER_MINUTE),
            sample(TelemetryMetric.THROTTLE_POSITION, throttle * PERCENT_SPAN, MetricUnit.PERCENT)
        )
        if (tick % SLOW_EVERY_TICKS != 0L) return fast

        return fast + listOf(
            sample(
                TelemetryMetric.ENGINE_TEMPERATURE,
                temperatureAt(seconds),
                MetricUnit.DEGREE_CELSIUS
            ),
            sample(
                TelemetryMetric.SYSTEM_VOLTAGE,
                voltageAt(engineSpeed),
                MetricUnit.VOLT
            ),
            sample(
                TelemetryMetric.VEHICLE_SPEED,
                throttle * SPEED_SPAN,
                MetricUnit.KILOMETER_PER_HOUR
            ),
            sample(
                TelemetryMetric.CALCULATED_ENGINE_LOAD,
                (IDLE_LOAD_PERCENT + throttle * LOAD_SPAN).coerceAtMost(PERCENT_SPAN),
                MetricUnit.PERCENT
            ),
            sample(
                TelemetryMetric.INTAKE_MANIFOLD_PRESSURE,
                IDLE_MANIFOLD_KPA + throttle * MANIFOLD_SPAN,
                MetricUnit.KILOPASCAL
            )
        )
    }

    private fun throttleAt(seconds: Double): Double {
        val wave = sin(TAU * seconds / THROTTLE_PERIOD_SECONDS)
        return max(0.0, wave) * max(0.0, wave)
    }

    private fun temperatureAt(seconds: Double): Double =
        AMBIENT_CELSIUS + TEMPERATURE_SPAN * (1 - exp(-seconds / WARMUP_TAU_SECONDS))

    private fun voltageAt(engineSpeed: Double): Double {
        val charging = ((engineSpeed - IDLE_RPM) / CHARGING_RPM_SPAN).coerceIn(0.0, 1.0)
        return RESTING_VOLTS + CHARGING_VOLT_SPAN * charging
    }

    private fun sample(metric: TelemetryMetric, value: Double, unit: MetricUnit) = TelemetrySample(
        metric = metric,
        value = value,
        unit = unit,
        source = TelemetrySource.SIMULATOR,
        monotonicMillis = clock.millis(),
        wallClockEpochMillis = wallClock()
    )

    private companion object {
        const val FAST_INTERVAL_MILLIS = 40L
        const val CONNECT_DELAY_MILLIS = 900L
        const val ECU_DELAY_MILLIS = 600L
        const val MILLIS_IN_SECOND = 1000.0
        const val SLOW_EVERY_TICKS = 24L
        const val TAU = 2 * Math.PI

        const val IDLE_RPM = 1250.0
        const val RPM_SPAN = 4300.0
        const val THROTTLE_PERIOD_SECONDS = 16.0
        const val PERCENT_SPAN = 100.0
        const val SPEED_SPAN = 96.0

        const val AMBIENT_CELSIUS = 27.0
        const val TEMPERATURE_SPAN = 68.0
        const val WARMUP_TAU_SECONDS = 70.0

        const val IDLE_LOAD_PERCENT = 14.0
        const val LOAD_SPAN = 82.0
        const val IDLE_MANIFOLD_KPA = 32.0
        const val MANIFOLD_SPAN = 66.0

        const val RESTING_VOLTS = 12.4
        const val CHARGING_VOLT_SPAN = 1.8
        const val CHARGING_RPM_SPAN = 1400.0

        const val DROPOUT_CYCLE_SECONDS = 75.0
        const val DROPOUT_START_SECONDS = 55.0
        const val DROPOUT_END_SECONDS = 62.0
    }
}
