package dev.br1ansouza.motoscope.core.history

import dev.br1ansouza.motoscope.core.model.MetricSummary
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionSummary
import java.io.OutputStream

enum class ExportFormat(val extension: String, val mediaType: String) {
    CSV("csv", "text/csv"),
    JSON("json", "application/json")
}

class SessionExporter(private val store: HistoryStore) {
    suspend fun write(id: SessionId, format: ExportFormat, destination: OutputStream) {
        val summary = store.summary(id) ?: throw NoSuchSessionException(id)
        val writer = destination.bufferedWriter()
        when (format) {
            ExportFormat.CSV -> writeCsv(id, writer)
            ExportFormat.JSON -> writeJson(id, summary, writer)
        }
        writer.flush()
    }

    private suspend fun writeCsv(id: SessionId, writer: Appendable) {
        writer.append(CSV_HEADER).append('\n')
        forEachSample(id) { stored ->
            val sample = stored.sample
            writer.append(escapeCsv(id.value)).append(',')
                .append(sample.metric.name).append(',')
                .append(sample.value.toString()).append(',')
                .append(sample.unit.name).append(',')
                .append(sample.source.name).append(',')
                .append(sample.monotonicMillis.toString()).append(',')
                .append(sample.wallClockEpochMillis.toString()).append('\n')
        }
    }

    private suspend fun writeJson(id: SessionId, summary: SessionSummary, writer: Appendable) {
        writer.append("{\"sessao\":").append(summary.session.toJson())
        writer.append(",\"totalDeAmostras\":").append(summary.sampleCount.toString())
        writer.append(",\"metricas\":[")
        summary.metrics.forEachIndexed { index, metric ->
            if (index > 0) writer.append(',')
            writer.append(metric.toJson())
        }
        writer.append("],\"eventos\":[")
        store.events(id).forEachIndexed { index, event ->
            if (index > 0) writer.append(',')
            writer.append(event.toJson())
        }
        writer.append("],\"amostras\":[")
        var first = true
        forEachSample(id) { stored ->
            if (!first) writer.append(',')
            first = false
            val sample = stored.sample
            writer.append("{\"metrica\":\"").append(sample.metric.name)
                .append("\",\"valor\":").append(sample.value.toString())
                .append(",\"unidade\":\"").append(sample.unit.name)
                .append("\",\"origem\":\"").append(sample.source.name)
                .append("\",\"monotonicoMs\":").append(sample.monotonicMillis.toString())
                .append(",\"horarioMs\":").append(sample.wallClockEpochMillis.toString())
                .append('}')
        }
        writer.append("]}")
    }

    private suspend fun forEachSample(id: SessionId, block: (StoredSample) -> Unit) {
        var cursor = 0L
        while (true) {
            val page = store.samplesAfter(id, cursor, PAGE_SIZE)
            if (page.isEmpty()) return
            page.forEach(block)
            cursor = page.last().rowId
        }
    }

    private companion object {
        const val PAGE_SIZE = 2_000
        const val CSV_HEADER =
            "sessao,metrica,valor,unidade,origem,monotonico_ms,horario_epoch_ms"
    }
}

class NoSuchSessionException(id: SessionId) :
    IllegalStateException("Sessão ${id.value} não existe.")

private fun RecordingSession.toJson(): String = buildString {
    append("{\"id\":\"").append(escapeJson(id.value))
    append("\",\"estado\":\"").append(status.name)
    append("\",\"inicioMs\":").append(startedAtEpochMillis)
    append(",\"fimMs\":").append(endedAtEpochMillis?.toString() ?: "null")
    append('}')
}

private fun MetricSummary.toJson(): String = buildString {
    append("{\"metrica\":\"").append(metric.name)
    append("\",\"unidade\":\"").append(unit.name)
    append("\",\"amostras\":").append(count)
    append(",\"minimo\":").append(minimum)
    append(",\"maximo\":").append(maximum)
    append(",\"media\":").append(average)
    append('}')
}

private fun SessionEvent.toJson(): String = buildString {
    append("{\"tipo\":\"").append(type.name)
    append("\",\"monotonicoMs\":").append(monotonicMillis)
    append(",\"horarioMs\":").append(wallClockEpochMillis)
    append(",\"detalhe\":")
    append(detail?.let { "\"" + escapeJson(it) + "\"" } ?: "null")
    append('}')
}

private val CSV_SPECIAL = charArrayOf(',', '"', '\n', '\r')

private fun escapeCsv(value: String): String = if (value.any { it in CSV_SPECIAL }) {
    "\"" + value.replace("\"", "\"\"") + "\""
} else {
    value
}

private fun escapeJson(value: String): String = buildString {
    value.forEach { character ->
        when {
            character == '"' -> append("\\\"")
            character == '\\' -> append("\\\\")
            character == '\n' -> append("\\n")
            character == '\r' -> append("\\r")
            character == '\t' -> append("\\t")
            character < ' ' -> append("\\u%04x".format(character.code))
            else -> append(character)
        }
    }
}
