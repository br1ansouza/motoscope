package dev.br1ansouza.motoscope.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MigrationTest {
    @Test
    fun everyHistoricalSchemaMigratesToCurrentWithoutLosingRows() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        for (version in 1..3) {
            val name = "migration-$version.db"
            context.deleteDatabase(name)
            createHistoricalDatabase(context, name, version)
            val database = Room.databaseBuilder(context, MotoScopeDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
            try {
                val sessions = database.sessionDao().observeSessions().first()
                assertEquals(2, sessions.size)
                assertEquals(200L, sessions.single { it.id == "finished" }.endedAtEpochMillis)
                assertEquals("FINISHED", sessions.single { it.id == "finished" }.status)
                val expected = if (version == 1) "INTERRUPTED" else "RECORDING"
                assertEquals(expected, sessions.single { it.id == "open" }.status)
                assertEquals(
                    if (version ==
                        1
                    ) {
                        0
                    } else {
                        1
                    },
                    database.sampleDao().countBySession("open")
                )
                assertEquals(
                    if (version ==
                        1
                    ) {
                        0
                    } else {
                        1
                    },
                    database.sessionEventDao().findBySession("open").size
                )
                database.openHelper.writableDatabase.query("PRAGMA foreign_key_check").use {
                    assertTrue(!it.moveToFirst())
                }
                if (version == 3) {
                    database.openHelper.writableDatabase.query(
                        "SELECT visible FROM metric_preferences WHERE metric = 'THROTTLE_POSITION'"
                    ).use {
                        assertTrue(it.moveToFirst())
                        assertEquals(0, it.getInt(0))
                    }
                }
            } finally {
                database.close()
                context.deleteDatabase(name)
            }
        }
    }

    private fun createHistoricalDatabase(context: Context, name: String, version: Int) {
        val schema = JSONObject(File("schemas/$SCHEMA_NAME/$version.json").readText())
            .getJSONObject("database")
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: JSONArray()
                for (i in 0 until indices.length()) {
                    val indexSql = indices.getJSONObject(i).getString("createSql")
                    db.execSQL(indexSql.replace("\${TABLE_NAME}", table))
                }
            }
            val statusColumn = if (version > 1) ", status" else ""
            val openStatus = if (version > 1) ", 'RECORDING'" else ""
            val finishedStatus = if (version > 1) ", 'FINISHED'" else ""
            db.execSQL(
                "INSERT INTO sessions " +
                    "(id, startedAtEpochMillis, endedAtEpochMillis$statusColumn) " +
                    "VALUES ('open', 100, NULL$openStatus)"
            )
            db.execSQL(
                "INSERT INTO sessions " +
                    "(id, startedAtEpochMillis, endedAtEpochMillis$statusColumn) " +
                    "VALUES ('finished', 100, 200$finishedStatus)"
            )
            if (version > 1) {
                db.execSQL(
                    "INSERT INTO telemetry_samples VALUES (1, 'open', 'ENGINE_RPM', 1000, " +
                        "'REVOLUTIONS_PER_MINUTE', 'ECU', 10, 110)"
                )
                db.execSQL(
                    "INSERT INTO session_events VALUES (1, 'open', 'RECORDING_STARTED', NULL, 10, 110)"
                )
            }
            if (version == 3) {
                db.execSQL("INSERT INTO metric_preferences VALUES ('THROTTLE_POSITION', 0)")
            }
            db.version = version
        }
    }

    private companion object {
        const val SCHEMA_NAME = "dev.br1ansouza.motoscope.core.database.MotoScopeDatabase"
    }
}
