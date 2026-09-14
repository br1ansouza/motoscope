package dev.br1ansouza.motoscope.feature.history

import dev.br1ansouza.motoscope.core.model.MetricUnit
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object HistoryFormatting {
    private val locale: Locale = Locale.forLanguageTag("pt-BR")

    private val whole: NumberFormat = NumberFormat.getIntegerInstance(locale)

    private val oneDecimal: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    private val stamp: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale)

    fun count(value: Long): String = whole.format(value)

    fun value(value: Double, unit: MetricUnit): String = when (unit) {
        MetricUnit.VOLT -> oneDecimal.format(value)
        MetricUnit.PERCENT -> oneDecimal.format(value)
        else -> oneDecimal.format(value)
    }

    fun startedAt(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        stamp.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun clock(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        TIME.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun duration(millis: Long): String {
        val totalSeconds = millis / MILLIS_PER_SECOND
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return if (hours > 0) {
            String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(locale, "%d:%02d", minutes, seconds)
        }
    }

    private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", locale)

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 3_600L
}
