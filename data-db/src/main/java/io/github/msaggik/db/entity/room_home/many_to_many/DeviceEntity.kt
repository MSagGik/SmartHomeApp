package io.github.msaggik.db.entity.room_home.many_to_many

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.msaggik.db.entity.config.DatabaseConfig

@Entity(tableName = DatabaseConfig.DEVICE_TABLE)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DatabaseConfig.DEVICE_ID)
    val idDevice: Long = 0L,
    @ColumnInfo(name = DatabaseConfig.DEVICE_MAC_ADDRESS)
    val macAddress: String,
    @ColumnInfo(name = DatabaseConfig.DEVICE_NAME)
    val name: String,
    @ColumnInfo(name = DatabaseConfig.DEVICE_TYPE)
    val typeDevice: Int,
    @ColumnInfo(name = DatabaseConfig.DEVICE_LIST_TYPE_IOT)
    val listTypeDeviceRoom: String,
    @ColumnInfo(name = DatabaseConfig.DEVICE_DATE_CREATE)
    var dateAdd: Long
)
