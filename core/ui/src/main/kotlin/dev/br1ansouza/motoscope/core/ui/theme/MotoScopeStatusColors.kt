package dev.br1ansouza.motoscope.core.ui.theme

import androidx.compose.ui.graphics.Color

object MotoScopePalette {
    val black = Color(0xFF0A0A0B)
    val graphite = Color(0xFF141518)
    val graphiteRaised = Color(0xFF1D1F23)
    val edge = Color(0xFF34373D)
    val darkRed = Color(0xFF6E1D22)
    val darkRedRaised = Color(0xFF8A252B)
    val gold = Color(0xFFC9A227)
    val ink = Color(0xFFEDEAE4)
}

object MotoScopeStatusColors {
    val ok = Color(0xFF6FA85C)
    val warning = Color(0xFFD8A32F)
    val failure = Color(0xFFD23B2E)
    val disabled = Color(0xFF5E6168)
    val recording = Color(0xFFD23B2E)
}

object MotoScopeRpmColors {
    val calm = MotoScopeStatusColors.ok
    val gold = MotoScopePalette.gold
    val redline = MotoScopeStatusColors.failure
}

object MotoScopeReadingColors {
    val fresh = MotoScopePalette.ink
    val delayed = MotoScopeStatusColors.warning
    val absent = MotoScopeStatusColors.disabled
}
