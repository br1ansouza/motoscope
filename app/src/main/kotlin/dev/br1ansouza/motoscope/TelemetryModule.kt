package dev.br1ansouza.motoscope

import android.os.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.br1ansouza.motoscope.core.model.FreshnessWindow
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.recording.RecordingEngine
import dev.br1ansouza.motoscope.core.recording.RecordingStore
import dev.br1ansouza.motoscope.core.recording.RecordingTuning
import dev.br1ansouza.motoscope.core.telemetry.MonotonicClock
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.telemetry.TelemetryFeed
import dev.br1ansouza.motoscope.core.telemetry.TelemetryHub
import dev.br1ansouza.motoscope.simulator.SimulatedTelemetryFeed
import java.util.UUID
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
internal object TelemetryModule {
    @Provides
    @Singleton
    fun provideClock(): MonotonicClock = MonotonicClock { SystemClock.elapsedRealtime() }

    @Provides
    @Singleton
    fun provideScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideHub(clock: MonotonicClock): TelemetryHub =
        TelemetryHub(SimulatedTelemetryFeed(clock))

    @Provides
    @Singleton
    fun provideFeed(hub: TelemetryHub): TelemetryFeed = hub

    @Provides
    @Singleton
    fun provideWindow(): FreshnessWindow = FreshnessWindow(
        delayedAfterMillis = DELAYED_AFTER_MILLIS,
        absentAfterMillis = ABSENT_AFTER_MILLIS
    )

    @Provides
    @Singleton
    fun provideEngine(
        feed: TelemetryFeed,
        clock: MonotonicClock,
        window: FreshnessWindow
    ): TelemetryEngine = TelemetryEngine(
        feed = feed,
        clock = clock,
        window = window,
        refreshIntervalMillis = REFRESH_INTERVAL_MILLIS
    )

    @Provides
    @Singleton
    fun provideRecordingEngine(
        feed: TelemetryFeed,
        store: RecordingStore,
        clock: MonotonicClock,
        window: FreshnessWindow
    ): RecordingEngine = RecordingEngine(
        events = feed.events(),
        store = store,
        clock = clock,
        wallClock = System::currentTimeMillis,
        newSessionId = { SessionId(UUID.randomUUID().toString()) },
        tuning = RecordingTuning(
            batchSize = BATCH_SIZE,
            flushIntervalMillis = FLUSH_INTERVAL_MILLIS,
            window = window
        )
    )

    private const val DELAYED_AFTER_MILLIS = 1_500L
    private const val ABSENT_AFTER_MILLIS = 4_000L
    private const val REFRESH_INTERVAL_MILLIS = 400L
    private const val BATCH_SIZE = 64
    private const val FLUSH_INTERVAL_MILLIS = 2_000L
}
