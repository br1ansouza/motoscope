package dev.br1ansouza.motoscope.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
internal data class SessionEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val status: String
)
