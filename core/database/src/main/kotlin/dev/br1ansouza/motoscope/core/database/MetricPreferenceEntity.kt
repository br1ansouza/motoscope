package dev.br1ansouza.motoscope.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metric_preferences")
internal data class MetricPreferenceEntity(@PrimaryKey val metric: String, val visible: Boolean)
