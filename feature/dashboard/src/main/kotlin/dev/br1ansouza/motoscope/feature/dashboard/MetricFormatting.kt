package dev.br1ansouza.motoscope.feature.dashboard

import dev.br1ansouza.motoscope.core.model.MetricUnit
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

internal object MetricFormatting {
    private val locale: Locale = Locale.forLanguageTag("pt-BR")

    private val whole: NumberFormat = NumberFormat.getIntegerInstance(locale)

    private val oneDecimal: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    fun format(value: Double, unit: MetricUnit): String = when (unit) {
        MetricUnit.VOLT -> oneDecimal.format(value)
        else -> whole.format(value.roundToInt())
    }
}
