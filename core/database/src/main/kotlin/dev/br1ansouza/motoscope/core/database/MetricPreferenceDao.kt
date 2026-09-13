package dev.br1ansouza.motoscope.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MetricPreferenceDao {
    @Upsert
    suspend fun upsert(preference: MetricPreferenceEntity)

    @Query("SELECT * FROM metric_preferences")
    fun observeAll(): Flow<List<MetricPreferenceEntity>>
}
