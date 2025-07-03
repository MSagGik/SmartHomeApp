package io.github.msaggik.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.msaggik.db.entity.config.DatabaseConfig
import io.github.msaggik.db.entity.room_home.additional_entities.RoomHomeWithDevicesEntity
import io.github.msaggik.db.entity.room_home.many_to_many.DeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeAndDeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeEntity

@Dao
interface RoomsHomeDao {
    // create room home and add devices in room
    @Transaction
    fun insertRoomHomeAndAddDevicesInRoomHome(roomHome: RoomHomeEntity, listDevices: List<DeviceEntity>): Long {
        val idRoomHome = insertRoomHome(roomHome)
        val devicesInRoomHome = getDevicesForRoomHome(idRoomHome)
        devicesInRoomHome.forEach { device ->
            val hasOtherRoomsHome = checkIfDeviceHasOtherRoomsHome(device.macAddress)
            if (!hasOtherRoomsHome) {
                deleteDevice(device)
            }
        }
        val listRoomHomeAndDevices = mutableListOf<RoomHomeAndDeviceEntity>()
        listDevices.forEach { device ->
            val idDevice = insertDevice(device)
            listRoomHomeAndDevices.add(
                RoomHomeAndDeviceEntity(
                    idRoomHome = idRoomHome,
                    idDevice = idDevice,
                    dateCreate = System.currentTimeMillis()
                )
            )
        }
        insertRoomHomeAndDevices(listRoomHomeAndDevices)
        return idRoomHome
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoomHome(roomHome: RoomHomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDevice(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoomHomeAndDevices(roomHomeAndDevices: List<RoomHomeAndDeviceEntity>): List<Long>

    // read rooms home and devices
    @Transaction
    @Query("SELECT * FROM ${DatabaseConfig.ROOM_HOME_TABLE}")
    fun listRoomsHomeWithDevices(): List<RoomHomeWithDevicesEntity>

    @Transaction
    @Query("SELECT * FROM ${DatabaseConfig.ROOM_HOME_TABLE} WHERE ${DatabaseConfig.ROOM_HOME_ID} = :roomHomeId")
    fun roomHomeWithDevices(roomHomeId: Long): RoomHomeWithDevicesEntity

    // delete room home
    @Transaction
    fun removeRoomHome(roomHomeId: Long): String {
        val currentRoomHome = getRoomInRoomsHome(roomHomeId)
        val devicesInRoomHome = getDevicesForRoomHome(roomHomeId)
        deleteRoomInRoomHomeAndDevices(roomHomeId)
        deleteRoomHomeInRooms(roomHomeId)
        devicesInRoomHome.forEach { device ->
            val hasOtherRoomsHome = checkIfDeviceHasOtherRoomsHome(device.macAddress)
            if (!hasOtherRoomsHome) {
                deleteDevice(device)
            }
        }
        return currentRoomHome.roomHomeImage ?: ""
    }

    @Query("SELECT * FROM ${DatabaseConfig.ROOM_HOME_TABLE} WHERE ${DatabaseConfig.ROOM_HOME_ID} = :roomHomeId")
    fun getRoomInRoomsHome(roomHomeId: Long): RoomHomeEntity

    @Transaction
    @Query("SELECT p.* FROM ${DatabaseConfig.DEVICE_TABLE} p JOIN ${DatabaseConfig.ROOM_HOME_AND_DEVICE_TABLE} np ON p.device_id = np.id_mac_device WHERE np.id_room_home = :roomHomeId ORDER BY ${DatabaseConfig.ROOM_HOME_AND_DEVICE_DATE_CREATE} DESC")
    fun getDevicesForRoomHome(roomHomeId: Long): List<DeviceEntity>

    @Query("DELETE FROM ${DatabaseConfig.ROOM_HOME_TABLE} WHERE ${DatabaseConfig.ROOM_HOME_ID} = :roomHomeId")
    fun deleteRoomHomeInRooms(roomHomeId: Long)

    @Query("SELECT COUNT(*) > 0 FROM ${DatabaseConfig.ROOM_HOME_AND_DEVICE_TABLE} WHERE ${DatabaseConfig.ID_MAC_DEVICE} = :deviceId")
    fun checkIfDeviceHasOtherRoomsHome(deviceId: String): Boolean

    @Query("DELETE FROM ${DatabaseConfig.ROOM_HOME_AND_DEVICE_TABLE} WHERE ${DatabaseConfig.ID_ROOM_HOME} = :roomId")
    fun deleteRoomInRoomHomeAndDevices(roomId: Long)

    @Delete(entity = DeviceEntity::class)
    fun deleteDevice(device: DeviceEntity): Int

    @Transaction
    fun deleteRoomsHomeAll(): Pair<Int, List<String>> {
        val clearImageUri = mutableListOf<String>()
        listRoomsHomeWithDevices().map { roomWithDevices ->
            roomWithDevices.roomHomeEntity.roomHomeImage?.let { roomHomeImageUri ->
                clearImageUri.add(roomHomeImageUri)
            }
        }
        val responseDeleteRoomHomeAndDevice = deleteRoomHomeAndDevice()
        val responseDeleteRoomsHome = deleteRoomsHome()
        val responseDeleteDevices = deleteDevices()
        return if (responseDeleteRoomHomeAndDevice == -1 || responseDeleteRoomsHome == -1 || responseDeleteDevices == -1) {
            Pair(-1, clearImageUri)
        } else {
            Pair(responseDeleteRoomHomeAndDevice, clearImageUri)
        }
    }

    @Query("DELETE FROM ${DatabaseConfig.ROOM_HOME_AND_DEVICE_TABLE}")
    fun deleteRoomHomeAndDevice(): Int

    @Query("DELETE FROM ${DatabaseConfig.ROOM_HOME_TABLE}")
    fun deleteRoomsHome(): Int

    @Query("DELETE FROM ${DatabaseConfig.DEVICE_TABLE}")
    fun deleteDevices(): Int
}