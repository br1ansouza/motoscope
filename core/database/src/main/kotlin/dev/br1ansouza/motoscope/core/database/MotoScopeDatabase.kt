package dev.br1ansouza.motoscope.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class], version = 1, exportSchema = true)
internal abstract class MotoScopeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
