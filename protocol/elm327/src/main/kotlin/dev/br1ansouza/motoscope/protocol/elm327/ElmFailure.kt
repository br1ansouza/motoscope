package dev.br1ansouza.motoscope.protocol.elm327

enum class ElmFailure {
    UNKNOWN_COMMAND,
    UNABLE_TO_CONNECT,
    BUS_INIT_FAILED,
    BUS_ERROR,
    CAN_ERROR,
    DATA_ERROR,
    BUFFER_FULL,
    STOPPED,
    LOW_POWER,
    GENERIC_ERROR
}
