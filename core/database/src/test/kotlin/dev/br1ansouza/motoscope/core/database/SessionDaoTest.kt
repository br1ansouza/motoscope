package dev.br1ansouza.motoscope.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
class SessionDaoTest {
    private lateinit var database: MotoScopeDatabase
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, MotoScopeDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase("recovery-test.db")
    }

    @Test
    fun newDatabaseHasNoInventedSessions() = runTest {
        assertTrue(database.sessionDao().observeSessions().first().isEmpty())
    }

    @Test
    fun sessionsAreObservedNewestFirst() = runTest {
        val dao = database.sessionDao()
        dao.insert(SessionEntity("older", 100))
        dao.insert(SessionEntity("newer", 200))

        assertEquals(listOf("newer", "older"), dao.observeSessions().first().map { it.id })
    }

    @Test
    fun finishingKeepsSessionAndExcludesItFromRecovery() = runTest {
        val dao = database.sessionDao()
        dao.insert(SessionEntity("session", 100))

        assertEquals(1, dao.finish("session", 200))
        assertTrue(dao.findUnfinished().isEmpty())
        assertEquals(200L, dao.observeSessions().first().single().endedAtEpochMillis)
        assertEquals(0, dao.finish("session", 300))
    }

    @Test
    fun finishRejectsUnknownSessionAndEndBeforeStart() = runTest {
        val dao = database.sessionDao()
        dao.insert(SessionEntity("session", 100))

        assertEquals(0, dao.finish("missing", 200))
        assertEquals(0, dao.finish("session", 99))
        assertEquals(listOf("session"), dao.findUnfinished().map { it.id })
    }

    @Test
    fun unfinishedSessionSurvivesDatabaseReopen() = runTest {
        database.close()
        database =
            Room.databaseBuilder(context, MotoScopeDatabase::class.java, "recovery-test.db").build()
        database.sessionDao().insert(SessionEntity("interrupted", 100))
        database.close()

        database =
            Room.databaseBuilder(context, MotoScopeDatabase::class.java, "recovery-test.db").build()

        assertEquals(
            listOf(SessionEntity("interrupted", 100)),
            database.sessionDao().findUnfinished()
        )
    }
}
