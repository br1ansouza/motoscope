package dev.br1ansouza.motoscope.protocol.elm327

sealed interface ElmResponse {
    data object Ok : ElmResponse

    data object NoData : ElmResponse

    data object Searching : ElmResponse

    data class Text(val value: String) : ElmResponse

    data class Data(val bytes: List<Int>) : ElmResponse {
        init {
            require(bytes.isNotEmpty()) { "Resposta de dados não pode ser vazia." }
            require(bytes.all { it in BYTE_MIN..BYTE_MAX }) { "Byte fora de faixa em $bytes." }
        }
    }

    data class Failure(val kind: ElmFailure, val raw: String) : ElmResponse

    data class Unhandled(val raw: String) : ElmResponse
}
