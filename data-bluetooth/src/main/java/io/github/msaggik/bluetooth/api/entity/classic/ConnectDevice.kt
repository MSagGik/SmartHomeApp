package io.github.msaggik.bluetooth.api.entity.classic

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream

/**
 * Data class representing a connected Bluetooth device along with its I/O and coroutine scopes.
 *
 * @property device The [BluetoothDevice] instance representing the remote Bluetooth device.
 * @property connectScope The [CoroutineScope] used for managing the lifecycle of the connection.
 * @property socket The [BluetoothSocket] used for communication; marked as [@Volatile] to handle visibility across threads.
 * @property inputConnectScope The [CoroutineScope] used for handling incoming data streams.
 * @property inputStream The [InputStream] for receiving data from the device.
 * @property outputStream The [OutputStream] for sending data to the device.
 */
data class ConnectDevice(
    var device: BluetoothDevice,
    var connectScope: CoroutineScope,
    @Volatile
    var socket: BluetoothSocket?,
    var inputConnectScope: CoroutineScope,
    var inputStream: InputStream?,
    var outputStream: OutputStream?
)
