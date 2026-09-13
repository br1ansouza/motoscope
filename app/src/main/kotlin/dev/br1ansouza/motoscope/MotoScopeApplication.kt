package dev.br1ansouza.motoscope

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MotoScopeApplication : Application() {
    @Inject
    internal lateinit var runtime: TelemetryRuntime

    override fun onCreate() {
        super.onCreate()
        runtime.start()
    }
}
