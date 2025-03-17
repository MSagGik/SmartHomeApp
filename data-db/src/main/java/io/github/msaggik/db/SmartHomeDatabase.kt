package io.github.msaggik.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.msaggik.db.converters.ColorsConverter
import io.github.msaggik.db.dao.LightingModeHomeDao
import io.github.msaggik.db.dao.RoomsHomeDao
import io.github.msaggik.db.entity.lighting_home.LightingModeEntity
import io.github.msaggik.db.entity.room_home.many_to_many.DeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeAndDeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeEntity

@Database(
    version = 1,
    entities = [
        RoomHomeEntity::class,
        DeviceEntity::class,
        RoomHomeAndDeviceEntity::class,
        LightingModeEntity::class
    ],
    exportSchema = true
)
@TypeConverters(ColorsConverter::class)
abstract class SmartHomeDatabase : RoomDatabase() {
    abstract fun roomsHomeDao(): RoomsHomeDao
    abstract fun lightingModeHomeDao(): LightingModeHomeDao
}