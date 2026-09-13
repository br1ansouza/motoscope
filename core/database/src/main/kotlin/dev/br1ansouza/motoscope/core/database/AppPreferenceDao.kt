package dev.br1ansouza.motoscope.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AppPreferenceDao {
    @Upsert
    suspend fun upsert(preference: AppPreferenceEntity)

    @Query("SELECT * FROM app_preferences WHERE `key` = :key")
    fun observe(key: String): Flow<AppPreferenceEntity?>
}
