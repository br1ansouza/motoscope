package dev.br1ansouza.motoscope.feature.recording

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RecordingModule {
    @Provides
    @Singleton
    fun provideNotifications(@ApplicationContext context: Context): RecordingNotifications =
        RecordingNotifications(context)
}
