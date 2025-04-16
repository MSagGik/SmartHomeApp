package io.github.msaggik.bluetooth.api.classic_poly

import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

/**
 * Interface for managing classic Bluetooth connections using custom connection logic.
 *
 * Provides methods to connect, send/receive data, manage device state, and observe connection updates.
 */
interface BluetoothConnectClassicPoly {
    /**
     * Attempts to connect to a Bluetooth device by its MAC address using the given UUID.
     * If the device is already connected, the success callback is called immediately.
     * Otherwise, it initiates a new connection.
     *
     * @param uuid The UUID representing the service to connect to.
     * @param macAddressDevice The MAC address of the Bluetooth device.
     * @param onSuccess Callback invoked when the connection is successful.
     * @param onError Callback invoked with an error message if the connection fails.
     */
    fun connect(uuid: UUID, macAddressDevice: String, onSuccess: () -> Unit, onError: (String) -> Unit)

    /**
     * Sends data to the specified connected Bluetooth device.
     *
     * @param data The data to send as a string.
     * @param macAddressDevice The MAC address of the target device.
     */
    fun sendData(macAddressDevice: String, data: ByteArray)
    fun sendListData(macAddressDevice: String, listData: List<ByteArray>)

    /**
     * Listens for incoming data from a specified connected Bluetooth device.
     * Calls a callback function every time a valid JSON string is received.
     *
     * @param macAddressDevice The MAC address of the device to listen to.
     * @param onDataReceived Callback triggered with the received string data.
     */
    fun receiveData(macAddressDevice: String, onDataReceived: (String) -> Unit): Boolean

    /**
     * Stops data reception for a specified device.
     *
     * @param macAddressDevice The MAC address of the device.
     */
    fun receiveDataClear(macAddressDevice: String)

    /**
     * Disconnects the device with the specified MAC address.
     *
     * @param macAddressDevice The MAC address of the device to disconnect.
     */
    fun disconnectDeviceViaMAC(macAddressDevice: String)

    /**
     * Disconnects all currently connected devices.
     */
    fun disconnectAllDevice()

    /**
     * Checks if the device with the given MAC address is connected.
     *
     * Searches for the device in the list of connected devices and checks its connection status.
     *
     * @param macAddressDevice the MAC address of the device to check.
     * @return a pair: `true` and the MAC address if the device is connected, otherwise `false` and the MAC address.
     */
    fun isConnected(macAddressDevice: String): Pair<Boolean, String?>

    /**
     * Returns a shared flow of MAC addresses for all currently connected devices.
     *
     * @return a [SharedFlow] emitting lists of connected device MAC addresses.
     */
    fun getConnectedDevices(): SharedFlow<List<String>>

    /**
     * Starts the process of periodically updating the list of connected devices.
     *
     * This method checks the connection status of devices at regular intervals and updates the connection state.
     * For each device, it checks whether it remains connected.
     */
    fun startDeviceConnectionUpdates()

    /**
     * Stops the process of updating the list of connected devices.
     * Cancels the ongoing update task.
     */
    fun stopDeviceConnectionUpdates()
}