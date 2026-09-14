package dev.br1ansouza.motoscope

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.br1ansouza.motoscope.core.history.ExportFormat
import dev.br1ansouza.motoscope.core.history.HistoryStore
import dev.br1ansouza.motoscope.core.history.SessionExporter
import dev.br1ansouza.motoscope.core.model.RecordingSession
import dev.br1ansouza.motoscope.core.model.SessionId
import dev.br1ansouza.motoscope.feature.history.ExportNotice
import dev.br1ansouza.motoscope.feature.history.HistoryState
import dev.br1ansouza.motoscope.feature.history.SessionDetailState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
internal class HistoryRuntime @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: HistoryStore,
    private val scope: CoroutineScope
) {
    private val exporter = SessionExporter(store)

    private val notice = MutableStateFlow<ExportNotice?>(null)

    val exportNotice: StateFlow<ExportNotice?> = notice.asStateFlow()

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

    fun export(id: SessionId, format: ExportFormat, destination: Uri) {
        notice.value = null
        scope.launch {
            val written = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = context.contentResolver.openOutputStream(destination)
                        ?: error("Sem fluxo de escrita para $destination.")
                    stream.use { exporter.write(id, format, it) }
                }.isSuccess
            }
            notice.value = if (written) ExportNotice.DONE else ExportNotice.FAILED
        }
    }

    fun clearExportNotice() {
        notice.value = null
    }
}
