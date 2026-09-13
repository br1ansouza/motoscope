package dev.br1ansouza.motoscope.core.vehicle

object VehicleCatalog {
    private val J_SERIES_350 = EngineProfile(
        displacementCc = 349,
        idleRpm = 1_050,
        torquePeakRpm = 4_000,
        powerPeakRpm = 6_100,
        revLimitRpm = 6_850,
        scaleMaxRpm = 7_000
    )

    val HUNTER_350 = j350("hunter-350", "Hunter 350")

    val ALL: List<VehicleProfile> = listOf(
        HUNTER_350,
        j350("classic-350", "Classic 350"),
        j350("meteor-350", "Meteor 350"),
        j350("bullet-350", "Bullet 350"),
        j350("goan-classic-350", "Goan Classic 350")
    )

    val DEFAULT: VehicleProfile = HUNTER_350

    fun byId(id: VehicleId): VehicleProfile? = ALL.firstOrNull { it.id == id }

    private fun j350(id: String, name: String) = VehicleProfile(
        id = VehicleId(id),
        displayName = name,
        engine = J_SERIES_350
    )
}
