package dev.br1ansouza.motoscope.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface SampleDao {
    @Insert
    suspend fun insertAll(samples: List<SampleEntity>)

    @Query("SELECT COUNT(*) FROM telemetry_samples WHERE sessionId = :sessionId")
    suspend fun countBySession(sessionId: String): Int

    @Query(
        "SELECT * FROM telemetry_samples WHERE sessionId = :sessionId " +
            "ORDER BY monotonicMillis ASC, id ASC"
    )
    suspend fun findBySession(sessionId: String): List<SampleEntity>
}
