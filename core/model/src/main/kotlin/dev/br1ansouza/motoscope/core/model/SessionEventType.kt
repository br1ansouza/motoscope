package dev.br1ansouza.motoscope.core.model

enum class SessionEventType {
    RECORDING_STARTED,
    RECORDING_RESUMED,
    RECORDING_INTERRUPTED,
    RECORDING_FINISHED,
    TRANSPORT_LOST,
    TRANSPORT_RECOVERED,
    DATA_GAP_STARTED,
    DATA_GAP_ENDED
}
