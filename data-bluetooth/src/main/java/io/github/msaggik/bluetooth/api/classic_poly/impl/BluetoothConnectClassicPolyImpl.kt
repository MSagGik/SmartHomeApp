package io.github.msaggik.bluetooth.api.classic_poly.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import io.github.msaggik.bluetooth.api.classic_poly.BluetoothConnectClassicPoly
import io.github.msaggik.bluetooth.api.entity.classic.ConnectDevice
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Lib BluetoothClassic"
private const val INTERVAL_UPDATE_STATUS_DEVICES = 1000L
private const val TIMEOUT_CONNECTED = 10_000L
private const val WAIT_WITH_NO_DATA = 100L
private const val DATA_BUFFER_SIZE = 1024

/**
 * Implements a classic Bluetooth connection (RFCOMM) handler.
 * Supports connecting, sending and receiving data, and managing active device connections.
 *
 * @property context Application context used for resource access.
 * @property bluetoothAdapter System Bluetooth adapter for managing connections.
 */
class BluetoothConnectClassicPolyImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothConnectClassicPoly {

    private val connectDevicesList: CopyOnWriteArrayList<ConnectDevice> = CopyOnWriteArrayList()

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
    override fun connect(
        uuid: UUID,
        macAddressDevice: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.i(TAG, "[connect] Request to connect to $macAddressDevice")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.let { connectDevice ->
            if (isActuallyConnectedDevice(connectDevice)) {
                Log.d(TAG, "[connect] Device already connected: ${connectDevice.device.address}")
                connectDevice.connectScope.launch {
                    withContext(Dispatchers.Main) { onSuccess() }
                }
            } else {
                Log.d(TAG, "[connect] Device found but not connected. Attempting reconnection")
                connectViaDevice(
                    uuid = uuid,
                    device = connectDevice,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } ?: run {
            Log.d(TAG, "[connect] Device not in list. Connecting by MAC")
            connectViaMAC(
                uuid = uuid,
                macAddressDevice = macAddressDevice,
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    /**
     * Attempts to establish a Bluetooth connection using a MAC address and UUID.
     * Creates and manages socket and IO streams. Adds device to the connection list on success.
     *
     * @param uuid The UUID of the Bluetooth service to connect to.
     * @param macAddressDevice The MAC address of the remote device.
     * @param onSuccess Callback called upon successful connection.
     * @param onError Callback with error message if the connection fails.
     */
    @SuppressLint("MissingPermission")
    private fun connectViaMAC(
        uuid: UUID,
        macAddressDevice: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.i(TAG, "[connectViaMAC] Connecting to $macAddressDevice")
        val mutex = Mutex()
        val connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        var socket: BluetoothSocket? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        connectionScope.launch {
            if (!mutex.tryLock()) {
                Log.w(TAG, "[connectViaMAC] Already attempting connection")
                withContext(Dispatchers.Main) { onError("Already connecting...") }
                return@launch
            }

            try {
                if (!bluetoothAdapter.isEnabled) {
                    Log.e(TAG, "[connectViaMAC] Bluetooth is disabled")
                    withContext(Dispatchers.Main) {
                        onError(context.getString(io.github.msaggik.ui.R.string.no_check_bluetooth))
                    }
                    return@launch
                }

                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }

                val device = bluetoothAdapter.getRemoteDevice(macAddressDevice)
                val tempSocket = device.createRfcommSocketToServiceRecord(uuid)

                withTimeoutOrNull(TIMEOUT_CONNECTED) {
                    if (bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                    tempSocket.connect()
                } ?: throw IOException("Connection timeout")

                socket = tempSocket
                inputStream = BufferedInputStream(socket?.inputStream)
                outputStream = socket?.outputStream

                val connectDevice = ConnectDevice(
                    device = device,
                    connectScope = connectionScope,
                    socket = socket,
                    inputConnectScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                    inputStream = inputStream,
                    outputStream = outputStream
                )

                connectDevicesList.add(connectDevice)
                Log.i(TAG, "[connectViaMAC] Connection established with $macAddressDevice")
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: IOException) {
                Log.e(TAG, "[connectViaMAC] Connection failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError(e.message ?: context.getString(io.github.msaggik.ui.R.string.error_connection))
                }
                connectDevicesList.removeAll { it.device.address == macAddressDevice }
                try {
                    socket?.close()
                    socket = null
                    inputStream = null
                    outputStream = null
                    Log.d(TAG, "[connectViaMAC] Disconnected successfully")
                } catch (closeEx: IOException) {
                    Log.e(TAG, "[connectViaMAC] Failed to close socket: ${closeEx.message}")
                }
                connectionScope.cancel()
            } finally {
                mutex.unlock()
            }
        }
    }

    /**
     * Attempts to reconnect using a known [ConnectDevice] instance.
     * Reinitializes socket and streams, and handles success or failure accordingly.
     *
     * @param uuid The UUID of the Bluetooth service.
     * @param device The already tracked [ConnectDevice] to reconnect.
     * @param onSuccess Callback called if connection is re-established.
     * @param onError Callback with error message if reconnection fails.
     */
    @SuppressLint("MissingPermission")
    private fun connectViaDevice(
        uuid: UUID,
        device: ConnectDevice,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.i(TAG, "[connectViaDevice] Attempting reconnection to ${device.device.address}")

        val mutex = Mutex()
        if (!device.connectScope.isActive) {
            device.connectScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        }
        device.connectScope.launch {
            if (!mutex.tryLock()) {
                Log.w(TAG, "[connectViaDevice] Connection already in progress")
                withContext(Dispatchers.Main) { onError("Already connecting...") }
                return@launch
            }

            try {
                if (!bluetoothAdapter.isEnabled) {
                    Log.e(TAG, "[connectViaDevice] Bluetooth is disabled")
                    withContext(Dispatchers.Main) {
                        onError(context.getString(io.github.msaggik.ui.R.string.no_check_bluetooth))
                    }
                    return@launch
                }

                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }

                device.device = bluetoothAdapter.getRemoteDevice(device.device.address)
                device.socket?.close()
                val tempSocket =
                    device.device.createRfcommSocketToServiceRecord(uuid)

                withTimeoutOrNull(TIMEOUT_CONNECTED) {
                    if (bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                    tempSocket.connect()
                } ?: throw IOException("Connection timeout")

                device.socket = tempSocket
                device.inputStream = BufferedInputStream(device.socket?.inputStream)
                device.outputStream = device.socket?.outputStream

                Log.i(TAG, "[connectViaDevice] Reconnection successful")
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: IOException) {
                Log.e(TAG, "[connectViaDevice] Connection failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError(e.message ?: context.getString(io.github.msaggik.ui.R.string.error_connection))
                }
                connectDevicesList.removeAll { it.device.address == device.device.address }
                disconnectDevice(device)
            } finally {
                mutex.unlock()
            }
        }
    }

    /**
     * Sends data to the specified connected Bluetooth device.
     *
     * @param data The data to send as a string.
     * @param macAddressDevice The MAC address of the target device.
     */
    override fun sendData(
        macAddressDevice: String,
        data: ByteArray
    ) {
        isTestDataSending.set(false)
        Log.i(TAG, "[sendData] Sending to $macAddressDevice: $data")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.let { deviceSendData ->
            deviceSendData.connectScope.launch {
                try {
                    if (deviceSendData.socket?.isConnected == true && deviceSendData.socket?.outputStream != null) {
                        deviceSendData.outputStream?.write(data)
                        deviceSendData.outputStream?.flush()
                        Log.i(TAG, "[sendData] Sending to ${deviceSendData.outputStream}")
                    } else {
                        Log.d(TAG, "[sendData] Attempting to send data when connection was broken")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "[sendData] Failed to send data: ${e.message}")
                    disconnectDeviceViaMAC(macAddressDevice)
                } finally {
                    isTestDataSending.set(true)
                }
            }
        } ?: Log.e(TAG, "[sendData] Device not found")
        isTestDataSending.set(true)
    }

    override fun sendListData(
        macAddressDevice: String,
        listData: List<ByteArray>
    ) {
        isTestDataSending.set(false)
        Log.i(TAG, "[sendData] Sending to $macAddressDevice: $listData")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.let { deviceSendData ->
            deviceSendData.connectScope.launch {
                try {
                    if (deviceSendData.socket?.isConnected == true && deviceSendData.socket?.outputStream != null) {
                        for(data in listData) {
                            deviceSendData.outputStream?.write(data)
                            deviceSendData.outputStream?.flush()
                            delay(100L)
                        }
                        Log.i(TAG, "[sendData] Sending to ${deviceSendData.outputStream}")
                    } else {
                        Log.d(TAG, "[sendData] Attempting to send data when connection was broken")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "[sendData] Failed to send data: ${e.message}")
                    disconnectDeviceViaMAC(macAddressDevice)
                } finally {
                    isTestDataSending.set(true)
                }
            }
        } ?: Log.e(TAG, "[sendData] Device not found")
        isTestDataSending.set(true)
    }

    /**
     * Listens for incoming data from a specified connected Bluetooth device.
     * Calls a callback function every time a valid JSON string is received.
     *
     * @param macAddressDevice The MAC address of the device to listen to.
     * @param onDataReceived Callback triggered with the received string data.
     */
    override fun receiveData(
        macAddressDevice: String,
        onDataReceived: (String) -> Unit
    ) : Boolean {
        Log.i(TAG, "[receiveData] Start received from $macAddressDevice")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.let { deviceReceiveData ->
            deviceReceiveData.connectScope.launch {
                deviceReceiveData.inputConnectScope.cancel()
                deviceReceiveData.inputConnectScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                deviceReceiveData.inputConnectScope.launch {
                    val buffer = ByteArray(DATA_BUFFER_SIZE)
                    val stringBuilder = StringBuilder()
                    try {
                        while (isActuallyConnectedDevice(deviceReceiveData)) {
                            if ((deviceReceiveData.inputStream?.available() ?: 0) > 0) {
                                val bytesRead = deviceReceiveData.inputStream?.read(buffer) ?: break
                                if (bytesRead > 0) {
                                    val data = buffer.copyOf(bytesRead)
                                    val jsonString = String(data, Charsets.UTF_8).trim()
                                    Log.d(TAG, "[receiveData] Received from $macAddressDevice: $jsonString")

                                    if (isCompleteJson(jsonString)) {
                                        withContext(Dispatchers.Main) {
                                            onDataReceived(jsonString)
                                        }
                                        stringBuilder.clear()
                                    } else {
                                        stringBuilder.append(jsonString)
                                        if (isCompleteJson(stringBuilder.toString())) {
                                            withContext(Dispatchers.Main) {
                                                onDataReceived(stringBuilder.toString())
                                            }
                                            stringBuilder.clear()
                                        }
                                    }
                                }
                            } else {
                                delay(WAIT_WITH_NO_DATA)
                            }
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "[receiveData] Error receiving data: ${e.message}")
                        disconnectDeviceViaMAC(macAddressDevice)
                    }
                }
            }
            return true
        } ?: run {
            Log.e(TAG, "[receiveData] Device not found")
            return false
        }
    }

    /**
     * Stops data reception for a specified device.
     *
     * @param macAddressDevice The MAC address of the device.
     */
    override fun receiveDataClear(macAddressDevice: String) {
        Log.i(TAG, "[receiveDataClear] $macAddressDevice")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.inputConnectScope?.cancel()
    }

    /**
     * Checks whether the provided string is a valid JSON object or JSON array.
     *
     * @param data the string to be checked.
     * @return `true` if the string is a valid JSON object or array, `false` otherwise.
     */
    private fun isCompleteJson(data: String): Boolean {
        return try {
            JSONObject(data)
            true
        } catch (e: JSONException) {
            try {
                JSONArray(data)
                true
            } catch (e: JSONException) {
                false
            }
        }
    }

    /**
     * Disconnects the specified connected device and closes all related resources.
     *
     * @param device The [ConnectDevice] instance to disconnect.
     */
    private fun disconnectDevice(device: ConnectDevice) {
        Log.i(TAG, "[disconnectDevice] start disconnectDevice $device")
        device.connectScope.launch {
            try {
                device.socket?.close()
                device.socket = null
                device.inputStream = null
                device.outputStream = null
                connectDevicesList.removeAll { it.device.address == device.device.address }
                Log.d(TAG, "[disconnectDevice] Disconnected successfully")
            } catch (e: IOException) {
                Log.e(TAG, "[disconnectDevice] Error closing connection: ${e.message}")
            } finally {
                device.inputConnectScope.cancel()
                device.connectScope.cancel()
                connectDevicesList.remove(device)
            }
        }
    }

    /**
     * Disconnects the device with the specified MAC address.
     *
     * @param macAddressDevice The MAC address of the device to disconnect.
     */
    override fun disconnectDeviceViaMAC(macAddressDevice: String) {
        Log.i(TAG, "[disconnectDeviceViaMAC] start disconnectDeviceViaMAC $macAddressDevice")
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        device?.let { deviceDisconnect ->
            deviceDisconnect.connectScope.launch {
                try {
                    deviceDisconnect.socket?.close()
                    deviceDisconnect.socket = null
                    deviceDisconnect.inputStream = null
                    deviceDisconnect.outputStream = null
                    Log.d(TAG, "[disconnectDeviceViaMAC] Disconnected successfully")
                } catch (e: IOException) {
                    Log.e(TAG, "[disconnectDeviceViaMAC] Error closing socket: ${e.message}")
                } finally {
                    deviceDisconnect.inputConnectScope.cancel()
                    deviceDisconnect.connectScope.cancel()
                    connectDevicesList.remove(deviceDisconnect)
                }
            }
        }
    }

    /**
     * Disconnects all currently connected devices.
     */
    override fun disconnectAllDevice() {
        Log.i(TAG, "[disconnectAllDevice] start disconnectAllDevice")
        connectDevicesList.toList().forEach {
            disconnectDevice(it)
        }
    }

    /**
     * Checks if the device with the given MAC address is connected.
     *
     * Searches for the device in the list of connected devices and checks its connection status.
     *
     * @param macAddressDevice the MAC address of the device to check.
     * @return a pair: `true` and the MAC address if the device is connected, otherwise `false` and the MAC address.
     */
    override fun isConnected(macAddressDevice: String): Pair<Boolean, String?> {
        val device = connectDevicesList.firstOrNull { it.device.address == macAddressDevice }
        return device?.let {
            Log.d(TAG, "[isConnected] Device $macAddressDevice is connected")
            Pair(isActuallyConnectedDevice(it), it.device.address)
        } ?: run {
            Log.e(TAG, "[isConnected] Device with MAC address $macAddressDevice not found in the connection list")
            Pair(false, macAddressDevice)
        }
    }

    private val _connectedDevices = MutableSharedFlow<List<String>>(replay = 1)
    override fun getConnectedDevices(): SharedFlow<List<String>> = _connectedDevices

    private var jobConnectedDevice: Job? = null

    /**
     * Starts the process of periodically updating the list of connected devices.
     *
     * This method checks the connection status of devices at regular intervals and updates the connection state.
     * For each device, it checks whether it remains connected.
     */
    override fun startDeviceConnectionUpdates() {
        Log.i(TAG, "[startDeviceConnectionUpdates] Starting device connection updates")
        jobConnectedDevice?.cancel()

        jobConnectedDevice = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                delay(INTERVAL_UPDATE_STATUS_DEVICES)
                val listMAC = mutableListOf<String>()
                Log.d(TAG, "[startDeviceConnectionUpdates] Checking device connections, current list: $connectDevicesList")
                connectDevicesList.forEach { connectDevice ->
                    if (isActuallyConnectedDevice(connectDevice) && isSocketAlive(connectDevice.socket)) {
                        listMAC.add(connectDevice.device.address)
                    } else {
                        connectDevicesList.removeAll { it.device.address == connectDevice.device.address }
                        Log.d(TAG, "[startDeviceConnectionUpdates] Device ${connectDevice.device.address} disconnected")
                    }
                }
                _connectedDevices.emit(listMAC)
            }
        }
    }

    /**
     * Stops the process of updating the list of connected devices.
     * Cancels the ongoing update task.
     */
    override fun stopDeviceConnectionUpdates() {
        Log.i(TAG, "[stopDeviceConnectionUpdates] Stopping device connection updates.")
        jobConnectedDevice?.cancel()
    }
    private val isTestDataSending = AtomicBoolean(true)
    /**
     * Checks if the socket is available for writing.
     *
     * Attempts to write a byte to the socket's output stream to check its activity.
     *
     * @param socket the socket to check.
     * @return `true` if the socket is active, `false` if an error occurs.
     */
    private fun isSocketAlive(socket: BluetoothSocket?): Boolean {
        return try {
            if (isTestDataSending.get()) {
                socket?.outputStream?.write("\n".toByteArray(Charsets.UTF_8))
                true
            } else {
                socket?.isConnected == true
            }
        } catch (e: IOException) {
            Log.w(TAG, "[isSocketAlive] Socket is not alive: ${e.message}")
            false
        }
    }

    /**
     * Checks if a device is connected, including checks for active sockets.
     *
     * Verifies that the device is active, the socket is connected, and both input/output streams are non-null.
     *
     * @param device the device to check.
     * @return `true` if the device is connected and ready for use, otherwise `false`.
     */
    private fun isActuallyConnectedDevice(device: ConnectDevice): Boolean {
        return device.connectScope.isActive
                && device.socket?.isConnected == true
                && device.inputStream != null
                && device.outputStream != null
    }
}