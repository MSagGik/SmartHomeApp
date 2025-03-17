package io.github.msaggik.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.msaggik.db.entity.config.DatabaseConfig
import io.github.msaggik.db.entity.lighting_home.LightingModeEntity

@Dao
interface LightingModeHomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLightingModeHome(lightingMode: LightingModeEntity): Long

    @Query("SELECT * FROM ${DatabaseConfig.LIGHTING_MODE_TABLE} WHERE ${DatabaseConfig.LIGHTING_MODE_ID} = :lightingModeId")
    fun getLightingModeHome(lightingModeId: Long): LightingModeEntity

    @Transaction
    @Query("SELECT * FROM ${DatabaseConfig.LIGHTING_MODE_TABLE} ORDER BY ${DatabaseConfig.LIGHTING_MODE_DATE_CREATE} DESC")
    fun listLightingModeHome(): List<LightingModeEntity>

    @Transaction
    fun removeLightingModeHomeById(lightingModeId: Long): String {
        val currentLightingMode = getLightingModeHome(lightingModeId)
        deleteLightingModeHomeById(lightingModeId)
        return currentLightingMode.uriImage ?: ""
    }

    @Transaction
    fun deleteLightingModeHomeAll(): Pair<Int, List<String>> {
        val clearImageUri = mutableListOf<String>()
        listLightingModeHome().map { lightingMode ->
            lightingMode.uriImage?.let { lightingModeImageUri ->
                clearImageUri.add(lightingModeImageUri)
            }
        }
        val responseDeleteAll = deleteAllLightingModeHome()
        return Pair(responseDeleteAll, clearImageUri)
    }

    @Query("DELETE FROM ${DatabaseConfig.LIGHTING_MODE_TABLE} WHERE ${DatabaseConfig.LIGHTING_MODE_ID} = :lightingModeId")
    fun deleteLightingModeHomeById(lightingModeId: Long)

    @Delete(entity = LightingModeEntity::class)
    fun deleteLightingModeHomeByEntity(lightingMode: LightingModeEntity): Int

    @Query("DELETE FROM ${DatabaseConfig.LIGHTING_MODE_TABLE}")
    fun deleteAllLightingModeHome(): Int
}