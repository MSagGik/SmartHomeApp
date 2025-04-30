package io.github.msaggik.home.data.mappers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import io.github.msaggik.db.entity.lighting_home.LightingModeEntity
import io.github.msaggik.db.entity.room_home.additional_entities.RoomHomeWithDevicesEntity
import io.github.msaggik.db.entity.room_home.many_to_many.DeviceEntity
import io.github.msaggik.db.entity.room_home.many_to_many.RoomHomeEntity
import io.github.msaggik.home.domain.model.device.ColorModel
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom
import io.github.msaggik.home.domain.model.device.state.TypeDeviceBluetooth
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.domain.model.room.RoomListHome

object HomeMappers {
    @SuppressLint("MissingPermission")
    fun map(bluetoothDevice: BluetoothDevice): SmartDeviceRoom {
        return with(bluetoothDevice) {
            SmartDeviceRoom(
                name = name,
                macAddress = address,
                typeDevice = when (bluetoothClass.majorDeviceClass) {
                    BluetoothClass.Device.Major.PHONE -> {
                        TypeDeviceBluetooth.Phone
                    }

                    BluetoothClass.Device.Major.COMPUTER -> {
                        TypeDeviceBluetooth.Computer
                    }

                    BluetoothClass.Device.Major.AUDIO_VIDEO -> {
                        TypeDeviceBluetooth.AudioAndVideo
                    }

                    else -> {
                        TypeDeviceBluetooth.Unknown
                    }
                }
            )
        }
    }

    fun map(roomHome: RoomHome): Pair<RoomHomeEntity, List<DeviceEntity>> {
        return with(roomHome) {
            val roomHomeEntity = RoomHomeEntity(
                idRoomHome = idRoomHome,
                roomHomeName = roomHomeName,
                roomHomeDescription = roomHomeDescription,
                roomHomeImage = roomHomeImage,
                dateModify = dateModify
            )
            val devicesEntity = mutableListOf<DeviceEntity>()
            listDevice.forEach { device ->
                val deviceEntity = DeviceEntity(
                    idDevice = device.idDevice,
                    macAddress = device.macAddress,
                    name = device.name,
                    typeDevice = device.typeDevice.number,
                    listTypeDeviceRoom = device.listTypeDeviceRoom.joinToString(separator = ",") { it.name },
                    dateAdd = device.dateAdd
                )
                devicesEntity.add(deviceEntity)
            }
            Pair(roomHomeEntity, devicesEntity)
        }
    }

    fun map(list: List<RoomHomeWithDevicesEntity>): RoomListHome {
        return RoomListHome(
            listRoom = list.map { map(it) }
        )
    }

    fun map(roomHomeWithDevicesEntity: RoomHomeWithDevicesEntity): RoomHome {
        return with(roomHomeWithDevicesEntity) {
            RoomHome(
                idRoomHome = roomHomeEntity.idRoomHome,
                roomHomeName = roomHomeEntity.roomHomeName,
                roomHomeDescription = roomHomeEntity.roomHomeDescription ?: "",
                roomHomeImage = roomHomeEntity.roomHomeImage,
                listDevice = devices.map { device -> map(device) },
                dateModify = roomHomeEntity.dateModify
            )
        }
    }

    fun map(deviceEntity: DeviceEntity): SmartDeviceRoom {
        return with(deviceEntity) {
            SmartDeviceRoom(
                idDevice = idDevice,
                name = name,
                macAddress = macAddress,
                typeDevice = when (typeDevice) {
                    TypeDeviceBluetooth.Phone.number -> TypeDeviceBluetooth.Phone
                    TypeDeviceBluetooth.Unknown.number -> TypeDeviceBluetooth.Unknown
                    TypeDeviceBluetooth.Computer.number -> TypeDeviceBluetooth.Computer
                    TypeDeviceBluetooth.AudioAndVideo.number -> TypeDeviceBluetooth.AudioAndVideo
                    else -> TypeDeviceBluetooth.Unknown
                },
                listTypeDeviceRoom = if (listTypeDeviceRoom.isEmpty()) {
                    mutableListOf()
                } else {
                    listTypeDeviceRoom.split(",").map { TypeDeviceRoom.valueOf(it) }.toMutableList()
                },
                dateAdd = dateAdd
            )
        }
    }

    fun map(lightingModePoly: LightingModePoly): LightingModeEntity {
        return with(lightingModePoly) {
            LightingModeEntity(
                id = id,
                name = name,
                uriImage = uriImage,
                colors = colors.map { map(it) },
                date = date
            )
        }
    }

    fun map(lightingMode: LightingModeEntity): LightingModePoly {
        return with(lightingMode) {
            LightingModePoly(
                id = id,
                name = name,
                uriImage = uriImage,
                colors = colors.map { map(it) },
                date = date
            )
        }
    }

    fun map(color: Int): ColorModel {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF
        return ColorModel(
            red = red,
            green = green,
            blue = blue
        )
    }

    private fun map(color: ColorModel): Int {
        val alpha = 255
        return (alpha shl 24) or (color.red shl 16) or (color.green shl 8) or color.blue
    }

    fun map(list: List<LightingModeEntity>): List<LightingModePoly> {
        return list.map { map(it) }
    }
}