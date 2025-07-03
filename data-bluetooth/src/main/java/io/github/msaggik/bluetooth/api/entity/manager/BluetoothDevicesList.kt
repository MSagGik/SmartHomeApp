package io.github.msaggik.bluetooth.api.entity.manager

import android.bluetooth.BluetoothDevice

/**
 * Subclass of [BluetoothDevicesResponse] that holds a list of discovered or paired [BluetoothDevice] instances.
 *
 * @param list The list of [BluetoothDevice] instances.
 */
data class BluetoothDevicesList (
    val list: List<BluetoothDevice>
) : BluetoothDevicesResponse()
