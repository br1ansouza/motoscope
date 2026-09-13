package dev.br1ansouza.motoscope.protocol.elm327

interface ElmTransport {
    suspend fun write(text: String)

    suspend fun read(): String
}
