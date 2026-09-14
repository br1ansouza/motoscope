package dev.br1ansouza.motoscope.feature.diagnostics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.br1ansouza.motoscope.protocol.elm327.ElmExchange
import dev.br1ansouza.motoscope.protocol.elm327.ElmResponse

@Composable
internal fun ElmExchange.describe(): String = when (this) {
    is ElmExchange.TimedOut -> stringResource(R.string.diagnostics_timed_out)
    is ElmExchange.Completed -> response.describe()
}

@Composable
internal fun ElmResponse.describe(): String = when (this) {
    ElmResponse.Ok -> "OK"
    ElmResponse.NoData -> "NO DATA"
    ElmResponse.Searching -> "SEARCHING"
    is ElmResponse.Text -> value
    is ElmResponse.Data -> bytes.joinToString(" ") { it.toHex() }
    is ElmResponse.Failure -> "${kind.name}: $raw"
    is ElmResponse.Unhandled -> raw
}

internal fun Int.toHex(): String = toString(HEX_RADIX).uppercase().padStart(2, '0')

private const val HEX_RADIX = 16
