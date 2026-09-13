package dev.br1ansouza.motoscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
import dev.br1ansouza.motoscope.core.telemetry.LiveTelemetry
import dev.br1ansouza.motoscope.core.telemetry.TelemetryEngine
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeTheme
import dev.br1ansouza.motoscope.feature.dashboard.DashboardScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var engine: TelemetryEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MotoScopeTheme {
                val telemetry = remember { engine.state() }
                val state by telemetry.collectAsState(initial = LiveTelemetry())
                DashboardScreen(state = state)
            }
        }
    }
}
