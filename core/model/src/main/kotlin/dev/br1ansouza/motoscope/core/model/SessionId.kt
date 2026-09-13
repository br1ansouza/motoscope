package dev.br1ansouza.motoscope.core.model

@JvmInline
value class SessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Identificador de sessão não pode ser vazio." }
    }
}
