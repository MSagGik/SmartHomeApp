package io.github.msaggik.home.domain.use_case.impl

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.repository.RepositoryDatabaseLightingMode
import io.github.msaggik.util.deleteImageFile
import io.github.msaggik.home.domain.use_case.InteractorDatabaseLightingMode

class InteractorDatabaseLightingModeImpl(
    val context: Context,
    private val repository: RepositoryDatabaseLightingMode
): InteractorDatabaseLightingMode {

    override suspend fun setLightingMode(lightingModePoly: LightingModePoly): Long {
        return repository.setLightingMode(lightingModePoly)
    }

    override fun getAllLightingMode(): Flow<List<LightingModePoly>> {
        return repository.getAllLightingMode()
    }

    override fun getLightingMode(lightingModeId: Long): Flow<LightingModePoly> {
        return repository.getLightingMode(lightingModeId)
    }

    override suspend fun removeLightingMode(lightingModeId: Long) {
        val cleanImageUri = repository.removeLightingMode(lightingModeId)
        if (cleanImageUri.isNotEmpty()) {
            context.deleteImageFile(cleanImageUri.toUri())
        }
    }

    override suspend fun deleteAllLightingMode(): Int {
        val pairResponse = repository.deleteAllLightingMode()
        if (pairResponse.second.isNotEmpty()) {
            for (imageUri in pairResponse.second) {
                context.deleteImageFile(imageUri.toUri())
            }
        }
        return pairResponse.first
    }
}