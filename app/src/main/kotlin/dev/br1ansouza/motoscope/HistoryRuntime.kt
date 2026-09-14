package dev.br1ansouza.motoscope

import dev.br1ansouza.motoscope.core.history.HistoryStore
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.feature.history.HistoryState
import dev.br1ansouza.motoscope.feature.history.SessionDetailState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
internal class HistoryRuntime @Inject constructor(
    private val store: HistoryStore,
    scope: CoroutineScope
) {
    val sessions: StateFlow<HistoryState> = store.sessions()
        .map<List<RecordingSession>, HistoryState> { HistoryState.Ready(it) }
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            HistoryState.Loading
        )

    suspend fun detail(id: SessionId): SessionDetailState {
        val summary = store.summary(id) ?: return SessionDetailState.Missing
        return SessionDetailState.Ready(summary = summary, events = store.events(id))
    }

    suspend fun delete(id: SessionId) {
        store.delete(id)
    }
}
