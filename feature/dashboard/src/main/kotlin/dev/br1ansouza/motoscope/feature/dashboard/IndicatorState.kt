package dev.br1ansouza.motoscope.feature.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.core.model.EcuState
import dev.br1ansouza.motoscope.core.model.TransportState
import dev.br1ansouza.motoscope.core.recording.RecordingState
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSizes
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeSpacing
import dev.br1ansouza.motoscope.core.ui.theme.MotoScopeStatusColors

internal data class IndicatorState(
    @param:StringRes val label: Int,
    @param:StringRes val detail: Int,
    val color: Color
)

@Composable
internal fun IndicatorRow(
    leading: List<IndicatorState>,
    trailing: List<IndicatorState>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IndicatorGroup(indicators = leading)
        IndicatorGroup(indicators = trailing)
    }
}

@Composable
private fun IndicatorGroup(indicators: List<IndicatorState>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        indicators.forEach { Indicator(it) }
    }
}

@Composable
private fun Indicator(state: IndicatorState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MotoScopeSpacing.tiny)
    ) {
        Box(
            modifier = Modifier
                .size(MotoScopeSizes.indicatorDot)
                .background(state.color, CircleShape)
        )
        Text(
            text = stringResource(state.label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(state.detail),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun TransportState.indicator(): IndicatorState = when (this) {
    TransportState.UNAVAILABLE -> IndicatorState(
        R.string.dashboard_indicator_transport,
        R.string.dashboard_transport_unavailable,
        MotoScopeStatusColors.disabled
    )

    TransportState.DISCONNECTED -> IndicatorState(
        R.string.dashboard_indicator_transport,
        R.string.dashboard_transport_disconnected,
        MotoScopeStatusColors.failure
    )

    TransportState.CONNECTING -> IndicatorState(
        R.string.dashboard_indicator_transport,
        R.string.dashboard_transport_connecting,
        MotoScopeStatusColors.warning
    )

    TransportState.RECONNECTING -> IndicatorState(
        R.string.dashboard_indicator_transport,
        R.string.dashboard_transport_reconnecting,
        MotoScopeStatusColors.warning
    )

    TransportState.CONNECTED -> IndicatorState(
        R.string.dashboard_indicator_transport,
        R.string.dashboard_transport_connected,
        MotoScopeStatusColors.ok
    )
}

internal fun EcuState.indicator(): IndicatorState = when (this) {
    EcuState.UNKNOWN -> IndicatorState(
        R.string.dashboard_indicator_ecu,
        R.string.dashboard_ecu_unknown,
        MotoScopeStatusColors.disabled
    )

    EcuState.NOT_RESPONDING -> IndicatorState(
        R.string.dashboard_indicator_ecu,
        R.string.dashboard_ecu_not_responding,
        MotoScopeStatusColors.failure
    )

    EcuState.DELAYED -> IndicatorState(
        R.string.dashboard_indicator_ecu,
        R.string.dashboard_ecu_delayed,
        MotoScopeStatusColors.warning
    )

    EcuState.RESPONDING -> IndicatorState(
        R.string.dashboard_indicator_ecu,
        R.string.dashboard_ecu_responding,
        MotoScopeStatusColors.ok
    )
}

internal fun RecordingState.indicator(): IndicatorState = when (this) {
    is RecordingState.Idle -> IndicatorState(
        R.string.dashboard_indicator_recording,
        R.string.dashboard_recording_idle,
        MotoScopeStatusColors.disabled
    )

    is RecordingState.Active -> IndicatorState(
        R.string.dashboard_indicator_recording,
        R.string.dashboard_recording_active,
        MotoScopeStatusColors.failure
    )
}
