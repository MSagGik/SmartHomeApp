package io.github.msaggik.db.entity.room_home.additional_entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import io.github.msaggik.db.entity.config.DatabaseConfig
import io.github.msaggik.db.entity.room_home.many_to_many.DeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeAndDeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeEntity

data class RoomHomeWithDevicesEntity(
    @Embedded
    val roomHomeEntity: RoomHomeEntity,
    @Relation(
        parentColumn = DatabaseConfig.ROOM_HOME_ID,
        entityColumn = DatabaseConfig.DEVICE_ID,
        entity = DeviceEntity::class,
        associateBy = Junction(RoomHomeAndDeviceEntity::class, parentColumn = DatabaseConfig.ID_ROOM_HOME, entityColumn = DatabaseConfig.ID_MAC_DEVICE)
    )
    val devices: List<DeviceEntity>
)
