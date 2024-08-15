package com.msaggik.data.api.bluetooth.impl

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.msaggik.data.api.bluetooth.BluetoothDevices
import com.msaggik.data.api.bluetooth.entity.BluetoothResponse
import com.msaggik.data.api.bluetooth.entity.BluetoothSmartDevice
import com.msaggik.data.api.bluetooth.entity.BluetoothSmartDeviceList

class BluetoothDevicesImpl (
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothDevices {

    override suspend fun getBluetoothDevices(): BluetoothResponse {
        val list: MutableList<BluetoothSmartDevice> = mutableListOf()

        if (!bluetoothAdapter.isEnabled) {
            return BluetoothResponse().apply { resultCode = 401 }
        } else if(ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return BluetoothResponse().apply { resultCode = 402 }
        }
        val bluetoothDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        for (bluetoothDevice in bluetoothDevices) {
            val device = BluetoothSmartDevice(
                name = bluetoothDevice.name,
                macAddress = bluetoothDevice.address,
                checked = false
            )
            list.add(device)
        }
        return BluetoothSmartDeviceList(list).apply { resultCode = 200 }
    }
}