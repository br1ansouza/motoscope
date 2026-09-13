package dev.br1ansouza.motoscope.core.vehicle

@JvmInline
value class VehicleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Identificador de moto não pode ser vazio." }
    }
}

data class VehicleProfile(val id: VehicleId, val displayName: String, val engine: EngineProfile) {
    init {
        require(displayName.isNotBlank()) { "Nome da moto não pode ser vazio." }
    }
}
