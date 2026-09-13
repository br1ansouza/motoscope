package dev.br1ansouza.motoscope.core.vehicle

data class EngineProfile(
    val displacementCc: Int,
    val idleRpm: Int,
    val torquePeakRpm: Int,
    val powerPeakRpm: Int,
    val revLimitRpm: Int,
    val scaleMaxRpm: Int
) {
    init {
        require(idleRpm > 0) { "Marcha lenta precisa ser positiva, recebida $idleRpm." }
        require(torquePeakRpm > idleRpm) { "Pico de torque precisa superar a marcha lenta." }
        require(powerPeakRpm > torquePeakRpm) { "Pico de potência precisa superar o de torque." }
        require(revLimitRpm > powerPeakRpm) { "Corte precisa superar o pico de potência." }
        require(scaleMaxRpm >= revLimitRpm) { "Escala precisa alcançar o corte." }
    }
}
