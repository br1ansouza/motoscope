package dev.br1ansouza.motoscope.feature.history

import dev.br1ansouza.motoscope.core.history.ExportFormat
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionEvent
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.core.model.SessionSummary

enum class ExportNotice {
    DONE,
    FAILED
}

sealed interface HistoryState {
    data object Loading : HistoryState

    data class Ready(val sessions: List<RecordingSession>) : HistoryState
}

sealed interface SessionDetailState {
    data object Loading : SessionDetailState

    data object Missing : SessionDetailState

    data class Ready(
        val summary: SessionSummary,
        val events: List<SessionEvent>
    ) : SessionDetailState
}

data class HistoryActions(
    val onOpenSession: (SessionId) -> Unit,
    val onDeleteSession: (SessionId) -> Unit,
    val onExportSession: (SessionId, ExportFormat) -> Unit,
    val onBack: () -> Unit
)
