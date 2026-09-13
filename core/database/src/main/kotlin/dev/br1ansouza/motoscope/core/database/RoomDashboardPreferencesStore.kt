package dev.br1ansouza.motoscope.core.database

import dev.br1ansouza.motoscope.core.settings.DashboardLayout
import dev.br1ansouza.motoscope.core.settings.DashboardPreferencesStore
import dev.br1ansouza.motoscope.core.vehicle.VehicleCatalog
import dev.br1ansouza.motoscope.core.vehicle.VehicleId
import dev.br1ansouza.motoscope.core.vehicle.VehicleProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomDashboardPreferencesStore @Inject constructor(
    private val dao: AppPreferenceDao
) : DashboardPreferencesStore {
    override fun layout(): Flow<DashboardLayout> = dao.observe(KEY_LAYOUT).map { stored ->
        DashboardLayout.entries.firstOrNull { it.name == stored?.value } ?: DEFAULT_LAYOUT
    }

    override suspend fun setLayout(layout: DashboardLayout) {
        dao.upsert(AppPreferenceEntity(KEY_LAYOUT, layout.name))
    }

    override fun vehicle(): Flow<VehicleProfile> = dao.observe(KEY_VEHICLE).map { stored ->
        stored?.value?.let { VehicleCatalog.byId(VehicleId(it)) } ?: VehicleCatalog.DEFAULT
    }

    override suspend fun setVehicle(profile: VehicleProfile) {
        dao.upsert(AppPreferenceEntity(KEY_VEHICLE, profile.id.value))
    }

    private companion object {
        const val KEY_LAYOUT = "dashboard.layout"
        const val KEY_VEHICLE = "vehicle.selected"
        val DEFAULT_LAYOUT = DashboardLayout.PRIMARY_TOP
    }
}
