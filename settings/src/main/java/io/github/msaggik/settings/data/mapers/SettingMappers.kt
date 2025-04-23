package io.github.msaggik.settings.data.mapers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.model.state.BluetoothType
import io.github.msaggik.settings.domain.model.state.BondState
import io.github.msaggik.settings.domain.model.state.TypeDeviceBluetooth

object SettingMappers {
    @SuppressLint("MissingPermission")
    fun map(bluetoothDevice: BluetoothDevice): SmartDevice {
        return with(bluetoothDevice) {
            SmartDevice(
                name = name,
                macAddress = address,
                typeDeviceBluetooth = when (bluetoothClass.majorDeviceClass) {
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
                },
                type = when (type) {
                    BluetoothDevice.DEVICE_TYPE_UNKNOWN -> {
                        BluetoothType.UNKNOWN
                    }
                    BluetoothDevice.DEVICE_TYPE_CLASSIC -> {
                        BluetoothType.CLASSIC_ONLY
                    }
                    BluetoothDevice.DEVICE_TYPE_LE -> {
                        BluetoothType.BLE_ONLY
                    }
                    BluetoothDevice.DEVICE_TYPE_DUAL -> {
                        BluetoothType.DUAL
                    }
                    else -> {
                        BluetoothType.ERROR
                    }
                },
                bondState = when (bondState) {
                    BluetoothDevice.BOND_NONE -> {
                        BondState.NOT_BONDED
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        BondState.BONDING
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        BondState.BONDED
                    }
                    else -> {
                        BondState.ERROR
                    }
                },
                uuids = uuids?.toList()
            )
        }
    }
}