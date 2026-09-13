package dev.br1ansouza.motoscope.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
internal data class AppPreferenceEntity(@PrimaryKey val key: String, val value: String)
