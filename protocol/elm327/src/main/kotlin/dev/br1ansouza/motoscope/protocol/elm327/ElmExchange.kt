package dev.br1ansouza.motoscope.protocol.elm327

sealed interface ElmExchange {
    val attempts: Int

    data class Completed(val response: ElmResponse, override val attempts: Int) : ElmExchange {
        init {
            require(attempts >= 1) { "Troca concluída precisa de ao menos uma tentativa." }
        }
    }

    data class TimedOut(override val attempts: Int) : ElmExchange {
        init {
            require(attempts >= 1) { "Troca expirada precisa de ao menos uma tentativa." }
        }
    }
}
