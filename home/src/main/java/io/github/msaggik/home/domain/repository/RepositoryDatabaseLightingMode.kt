package io.github.msaggik.home.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.device.LightingModePoly

interface RepositoryDatabaseLightingMode {
    suspend fun setLightingMode(lightingModePoly: LightingModePoly): Long
    fun getAllLightingMode(): Flow<List<LightingModePoly>>
    fun getLightingMode(lightingModeId: Long): Flow<LightingModePoly>
    suspend fun removeLightingMode(lightingModeId: Long): String
    suspend fun deleteAllLightingMode(): Pair<Int, List<String>>
}