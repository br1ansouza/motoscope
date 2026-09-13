package dev.br1ansouza.motoscope.core.database

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.br1ansouza.motoscope.core.recording.RecordingStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MotoScopeDatabase =
        Room.databaseBuilder(context, MotoScopeDatabase::class.java, "motoscope.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideSessionDao(database: MotoScopeDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideSampleDao(database: MotoScopeDatabase): SampleDao = database.sampleDao()

    @Provides
    fun provideSessionEventDao(database: MotoScopeDatabase): SessionEventDao =
        database.sessionEventDao()
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecordingStoreModule {
    @Binds
    abstract fun bindRecordingStore(store: RoomRecordingStore): RecordingStore
}
