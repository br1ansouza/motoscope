package dev.br1ansouza.motoscope.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TelemetrySampleTest {
    @Test
    fun nonFiniteReadingIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { sampleWithValue(Double.NaN) }
        assertThrows(IllegalArgumentException::class.java) {
            sampleWithValue(Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun negativeMonotonicInstantIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            sampleWithValue(3000.0, monotonicMillis = -1)
        }
    }

    @Test
    fun ageIsMeasuredOnTheMonotonicBase() {
        val sample = sampleWithValue(3000.0, monotonicMillis = 1_000)

        assertEquals(250, sample.ageMillisAt(1_250))
        assertEquals(0, sample.ageMillisAt(1_000))
    }

    @Test
    fun ageBeforeTheSampleIsRejected() {
        val sample = sampleWithValue(3000.0, monotonicMillis = 1_000)

        assertThrows(IllegalArgumentException::class.java) { sample.ageMillisAt(999) }
    }

    @Test
    fun sameMetricFromDifferentSourcesStaysDistinct() {
        val fromEcu = TelemetrySample(
            metric = TelemetryMetric.SYSTEM_VOLTAGE,
            value = 13.9,
            unit = MetricUnit.VOLT,
            source = TelemetrySource.ECU,
            monotonicMillis = 1_000,
            wallClockEpochMillis = 1_700_000_000_000
        )

        assertNotEquals(fromEcu, fromEcu.copy(source = TelemetrySource.ADAPTER))
    }

    private fun sampleWithValue(value: Double, monotonicMillis: Long = 1_000) = TelemetrySample(
        metric = TelemetryMetric.ENGINE_RPM,
        value = value,
        unit = MetricUnit.REVOLUTIONS_PER_MINUTE,
        source = TelemetrySource.ECU,
        monotonicMillis = monotonicMillis,
        wallClockEpochMillis = 1_700_000_000_000
    )
}
