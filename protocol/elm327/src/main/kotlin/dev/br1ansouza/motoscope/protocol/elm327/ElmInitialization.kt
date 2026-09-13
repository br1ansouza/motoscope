package dev.br1ansouza.motoscope.protocol.elm327

object ElmInitialization {
    val DEFAULT: List<ElmCommand> = listOf(
        ElmCommand.RESET,
        ElmCommand.ECHO_OFF,
        ElmCommand.LINEFEEDS_OFF,
        ElmCommand.SPACES_OFF,
        ElmCommand.HEADERS_OFF,
        ElmCommand.AUTO_PROTOCOL,
        ElmCommand.DESCRIBE_PROTOCOL
    )
}
