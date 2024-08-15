package com.msaggik.data.api.bluetooth

import com.msaggik.data.api.bluetooth.entity.BluetoothResponse

interface BluetoothDevices {
    suspend fun getBluetoothDevices() : BluetoothResponse
}