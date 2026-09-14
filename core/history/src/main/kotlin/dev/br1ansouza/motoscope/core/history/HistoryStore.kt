package dev.br1ansouza.motoscope.core.history

import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionSummary
import dev.br1ansouza.motoscope.core.model.TelemetrySample
import kotlinx.coroutines.flow.Flow

data class StoredSample(val rowId: Long, val sample: TelemetrySample) {
    init {
        require(rowId > 0) { "Identificador de amostra precisa ser positivo, recebido $rowId." }
    }
}

interface HistoryStore {
    fun sessions(): Flow<List<RecordingSession>>

    suspend fun summary(id: SessionId): SessionSummary?

    suspend fun events(id: SessionId): List<SessionEvent>

    suspend fun samplesAfter(id: SessionId, rowId: Long, limit: Int): List<StoredSample>

    suspend fun delete(id: SessionId)
}
