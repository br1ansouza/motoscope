package dev.br1ansouza.motoscope.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FreshnessWindowTest {
    private val window = FreshnessWindow(delayedAfterMillis = 500, absentAfterMillis = 2_000)

    @Test
    fun readingIsFreshBeforeTheDelayLimit() {
        assertEquals(Freshness.FRESH, window.classify(0))
        assertEquals(Freshness.FRESH, window.classify(499))
    }

    @Test
    fun delayLimitIsInclusive() {
        assertEquals(Freshness.DELAYED, window.classify(500))
        assertEquals(Freshness.DELAYED, window.classify(1_999))
    }

    @Test
    fun absenceLimitIsInclusive() {
        assertEquals(Freshness.ABSENT, window.classify(2_000))
        assertEquals(Freshness.ABSENT, window.classify(60_000))
    }

    @Test
    fun negativeAgeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { window.classify(-1) }
    }

    @Test
    fun windowWithoutOrderedLimitsIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FreshnessWindow(delayedAfterMillis = 2_000, absentAfterMillis = 500)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FreshnessWindow(delayedAfterMillis = 500, absentAfterMillis = 500)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FreshnessWindow(delayedAfterMillis = 0, absentAfterMillis = 500)
        }
    }

    @Test
    fun sampleAgesWithoutFreezingAtTheLastValue() {
        val sample = TelemetrySample(
            metric = TelemetryMetric.ENGINE_RPM,
            value = 3_420.0,
            unit = MetricUnit.REVOLUTIONS_PER_MINUTE,
            source = TelemetrySource.ECU,
            monotonicMillis = 10_000,
            wallClockEpochMillis = 1_700_000_000_000
        )

        assertEquals(Freshness.FRESH, window.classify(sample, 10_100))
        assertEquals(Freshness.DELAYED, window.classify(sample, 10_700))
        assertEquals(Freshness.ABSENT, window.classify(sample, 12_000))
    }
}
