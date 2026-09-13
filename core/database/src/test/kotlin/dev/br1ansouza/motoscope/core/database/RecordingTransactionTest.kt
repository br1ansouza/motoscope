package dev.br1ansouza.motoscope.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.br1ansouza.motoscope.core.model.MetricUnit
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionEventType
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionStatus
import dev.br1ansouza.motoscope.core.model.TelemetryMetric
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import dev.br1ansouza.motoscope.core.model.TelemetrySource
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RecordingTransactionTest {
    private lateinit var database: MotoScopeDatabase
    private lateinit var store: RoomRecordingStore
    private val id = SessionId("test-session")
    private val sample =
        TelemetrySample(
            TelemetryMetric.ENGINE_RPM,
            1000.0,
            MetricUnit.REVOLUTIONS_PER_MINUTE,
            TelemetrySource.SIMULATOR,
            10,
            110
        )

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MotoScopeDatabase::class.java).build()
        store =
            RoomRecordingStore(
                database,
                database.sessionDao(),
                database.sampleDao(),
                database.sessionEventDao()
            )
    }

    @After
    fun close() = database.close()

    @Test
    fun failedFinalEventRollsBackSamplesAndStatusThenRetryWritesOnce() = runTest {
        store.createSession(
            RecordingSession(id, SessionStatus.RECORDING, 100),
            event(SessionEventType.RECORDING_STARTED)
        )
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_final BEFORE INSERT ON session_events WHEN NEW.type = " +
                "'RECORDING_FINISHED' BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        val result = runCatching {
            store.completeSession(
                id,
                listOf(sample),
                event(SessionEventType.RECORDING_FINISHED),
                SessionStatus.FINISHED
            )
        }
        assertTrue(result.isFailure)
        assertEquals(0, database.sampleDao().countBySession(id.value))
        assertEquals("RECORDING", database.sessionDao().findUnfinished().single().status)
        assertEquals(1, database.sessionEventDao().findBySession(id.value).size)
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_final")
        store.completeSession(
            id,
            listOf(sample),
            event(SessionEventType.RECORDING_FINISHED),
            SessionStatus.FINISHED
        )
        assertEquals(1, database.sampleDao().countBySession(id.value))
        assertEquals("FINISHED", database.sessionDao().observeSessions().first().single().status)
        assertEquals(2, database.sessionEventDao().findBySession(id.value).size)
    }

    @Test
    fun missingSessionCannotSilentlyFinish() = runTest {
        assertTrue(
            runCatching {
                store.completeSession(
                    id,
                    emptyList(),
                    event(SessionEventType.RECORDING_FINISHED),
                    SessionStatus.FINISHED
                )
            }.isFailure
        )
    }

    @Test
    fun recoveryPreservesRowsAndResumesSameSessionWithPersistedCount() = runTest {
        store.createSession(
            RecordingSession(id, SessionStatus.RECORDING, 100),
            event(SessionEventType.RECORDING_STARTED)
        )
        store.appendSamples(id, listOf(sample))
        store.markUnfinishedInterrupted()
        assertEquals(SessionStatus.INTERRUPTED, store.findUnfinished().single().status)
        assertEquals(1L, store.resumeSession(id, event(SessionEventType.RECORDING_RESUMED)))
        assertEquals(SessionStatus.RECORDING, store.findUnfinished().single().status)
    }

    @Test
    fun oneHourAtSimulationPeakPersistsInBoundedBatches() = runTest {
        store.createSession(
            RecordingSession(id, SessionStatus.RECORDING, 100),
            event(SessionEventType.RECORDING_STARTED)
        )
        val total = 200_000
        var written = 0
        val started = System.nanoTime()
        while (written < total) {
            val batch = List(minOf(128, total - written)) { index ->
                sample.copy(monotonicMillis = (written + index).toLong())
            }
            store.appendSamples(id, batch)
            written += batch.size
        }
        assertEquals(total, database.sampleDao().countBySession(id.value))
        val sqlite = database.openHelper.writableDatabase
        val pages = sqlite.query("PRAGMA page_count").use {
            it.moveToFirst()
            it.getLong(0)
        }
        val pageSize = sqlite.query("PRAGMA page_size").use {
            it.moveToFirst()
            it.getLong(0)
        }
        val elapsedMillis = (System.nanoTime() - started) / 1_000_000
        val report = File("build/reports/recording-volume.txt")
        checkNotNull(report.parentFile).mkdirs()
        report.writeText(
            "Synthetic JVM/SQLite run: $total rows, ${pages * pageSize} bytes, $elapsedMillis ms.\n"
        )
    }

    private fun event(type: SessionEventType) = SessionEvent(type, 20, 120)
}
