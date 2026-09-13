package dev.br1ansouza.motoscope.core.recording

import dev.br1ansouza.motoscope.core.model.RecordingSession

sealed interface RecordingState {
    data object Idle : RecordingState

    data class Active(
        val session: RecordingSession,
        val samplesWritten: Long,
        val samplesPending: Int
    ) : RecordingState
}
