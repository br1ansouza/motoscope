package dev.br1ansouza.motoscope.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startedAtEpochMillis DESC, id ASC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Query(
        "SELECT * FROM sessions WHERE endedAtEpochMillis IS NULL ORDER BY startedAtEpochMillis ASC, id ASC"
    )
    suspend fun findUnfinished(): List<SessionEntity>

    @Query(
        "UPDATE sessions SET endedAtEpochMillis = :endedAtEpochMillis, status = :status " +
            "WHERE id = :id AND endedAtEpochMillis IS NULL AND startedAtEpochMillis <= :endedAtEpochMillis"
    )
    suspend fun finish(id: String, endedAtEpochMillis: Long, status: String): Int

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String): Int
}
