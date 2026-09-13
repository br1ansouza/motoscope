package dev.br1ansouza.motoscope.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface SessionEventDao {
    @Insert
    suspend fun insert(event: SessionEventEntity)

    @Query(
        "SELECT * FROM session_events WHERE sessionId = :sessionId " +
            "ORDER BY monotonicMillis ASC, id ASC"
    )
    suspend fun findBySession(sessionId: String): List<SessionEventEntity>
}
