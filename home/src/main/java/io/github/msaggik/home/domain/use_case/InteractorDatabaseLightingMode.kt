package io.github.msaggik.home.domain.use_case

import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.device.LightingModePoly

interface InteractorDatabaseLightingMode {
    suspend fun setLightingMode(lightingModePoly: LightingModePoly): Long
    fun getAllLightingMode(): Flow<List<LightingModePoly>>
    fun getLightingMode(lightingModeId: Long): Flow<LightingModePoly>
    suspend fun removeLightingMode(lightingModeId: Long)
    suspend fun deleteAllLightingMode(): Int
}