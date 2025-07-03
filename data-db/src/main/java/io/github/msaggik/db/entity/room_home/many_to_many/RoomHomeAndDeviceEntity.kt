package io.github.msaggik.db.entity.room_home.many_to_many

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import io.github.msaggik.db.entity.config.DatabaseConfig

@Entity(
    tableName = DatabaseConfig.ROOM_HOME_AND_DEVICE_TABLE,
    primaryKeys = [DatabaseConfig.ID_ROOM_HOME, DatabaseConfig.ID_MAC_DEVICE],
    indices = [
        Index(value = [DatabaseConfig.ID_MAC_DEVICE]),
        Index(value = [DatabaseConfig.ID_ROOM_HOME])
    ]
)
data class RoomHomeAndDeviceEntity(
    @ColumnInfo(name = DatabaseConfig.ID_ROOM_HOME)
    val idRoomHome: Long,
    @ColumnInfo(name = DatabaseConfig.ID_MAC_DEVICE)
    val idDevice: Long,
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_AND_DEVICE_DATE_CREATE)
    val dateCreate: Long = -1L
)
