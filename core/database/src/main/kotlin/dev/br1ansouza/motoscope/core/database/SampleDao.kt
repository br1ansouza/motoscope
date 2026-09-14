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

    @Query(
        "SELECT metric AS metric, unit AS unit, COUNT(*) AS count, MIN(value) AS minimum, " +
            "MAX(value) AS maximum, AVG(value) AS average FROM telemetry_samples " +
            "WHERE sessionId = :sessionId GROUP BY metric, unit ORDER BY metric ASC"
    )
    suspend fun summarize(sessionId: String): List<MetricSummaryRow>

    @Query(
        "SELECT * FROM telemetry_samples WHERE sessionId = :sessionId AND id > :rowId " +
            "ORDER BY id ASC LIMIT :limit"
    )
    suspend fun page(sessionId: String, rowId: Long, limit: Int): List<SampleEntity>
}

internal data class MetricSummaryRow(
    val metric: String,
    val unit: String,
    val count: Long,
    val minimum: Double,
    val maximum: Double,
    val average: Double
)
