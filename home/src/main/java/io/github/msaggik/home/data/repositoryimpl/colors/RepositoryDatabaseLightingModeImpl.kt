package io.github.msaggik.home.data.repositoryimpl.colors

import io.github.msaggik.db.SmartHomeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.msaggik.home.data.mappers.HomeMappers
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.repository.RepositoryDatabaseLightingMode

class RepositoryDatabaseLightingModeImpl(
    private val db: SmartHomeDatabase
) : RepositoryDatabaseLightingMode {

    override suspend fun setLightingMode(lightingModePoly: LightingModePoly): Long {
        return db.lightingModeHomeDao().insertLightingModeHome(HomeMappers.map(lightingModePoly))
    }

    override fun getAllLightingMode(): Flow<List<LightingModePoly>> = flow {
        emit(
            HomeMappers.map(list = db.lightingModeHomeDao().listLightingModeHome())
        )
    }

    override fun getLightingMode(lightingModeId: Long): Flow<LightingModePoly> = flow {
        emit(
            HomeMappers.map(
                lightingMode = db.lightingModeHomeDao().getLightingModeHome(lightingModeId)
            )
        )
    }

    override suspend fun removeLightingMode(lightingModeId: Long): String {
        return db.lightingModeHomeDao().removeLightingModeHomeById(lightingModeId)
    }

    override suspend fun deleteAllLightingMode(): Pair<Int, List<String>> {
        return db.lightingModeHomeDao().deleteLightingModeHomeAll()
    }
}