package io.github.msaggik.db.entity.lighting_home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.msaggik.db.entity.config.DatabaseConfig

@Entity(tableName = DatabaseConfig.LIGHTING_MODE_TABLE)
data class LightingModeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DatabaseConfig.LIGHTING_MODE_ID)
    val id: Long = 0L,
    @ColumnInfo(name = DatabaseConfig.LIGHTING_MODE_NAME)
    val name: String,
    @ColumnInfo(name = DatabaseConfig.LIGHTING_MODE_IMAGE_URI)
    val uriImage: String?,
    @ColumnInfo(name = DatabaseConfig.LIGHTING_MODE_COLORS)
    val colors: List<Int>,
    @ColumnInfo(name = DatabaseConfig.LIGHTING_MODE_DATE_CREATE)
    val date: Long
)
