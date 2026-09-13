package dev.br1ansouza.motoscope.core.settings

import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile
import kotlinx.coroutines.flow.Flow

interface DashboardPreferencesStore {
    fun layout(): Flow<DashboardLayout>

    suspend fun setLayout(layout: DashboardLayout)

    fun vehicle(): Flow<VehicleProfile>

    suspend fun setVehicle(profile: VehicleProfile)
}
