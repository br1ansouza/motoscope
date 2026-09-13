package dev.br1ansouza.motoscope.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, SampleEntity::class, SessionEventEntity::class],
    version = 2,
    exportSchema = true
)
internal abstract class MotoScopeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun sampleDao(): SampleDao

    abstract fun sessionEventDao(): SessionEventDao
}
