package dev.br1ansouza.motoscope

import android.os.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.br1ansouza.motoscope.core.model.FreshnessWindow
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.telemetry.TelemetryFeed
import dev.br1ansouza.motoscope.simulator.SimulatedTelemetryFeed
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TelemetryModule {
    @Provides
    @Singleton
    fun provideClock(): MonotonicClock = MonotonicClock { SystemClock.elapsedRealtime() }

    @Provides
    @Singleton
    fun provideFeed(clock: MonotonicClock): TelemetryFeed = SimulatedTelemetryFeed(clock)

    @Provides
    @Singleton
    fun provideEngine(feed: TelemetryFeed, clock: MonotonicClock): TelemetryEngine =
        TelemetryEngine(
            feed = feed,
            clock = clock,
            window = FreshnessWindow(
                delayedAfterMillis = DELAYED_AFTER_MILLIS,
                absentAfterMillis = ABSENT_AFTER_MILLIS
            ),
            refreshIntervalMillis = REFRESH_INTERVAL_MILLIS
        )

    private const val DELAYED_AFTER_MILLIS = 1_500L
    private const val ABSENT_AFTER_MILLIS = 4_000L
    private const val REFRESH_INTERVAL_MILLIS = 400L
}
