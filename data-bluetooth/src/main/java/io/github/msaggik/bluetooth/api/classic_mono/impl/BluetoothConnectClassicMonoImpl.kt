package io.github.msaggik.bluetooth.api.classic_mono.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import io.github.msaggik.bluetooth.api.classic_mono.BluetoothConnectClassicMono
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

private const val DEFAULT_UUID =
    "00001101-0000-1000-8000-00805F9B34FB" // номер UUID последовательного порта Bluetooth (Serial Port Profile, SPP)
private const val INTERVAL_UPDATE_STATUS_DEVICES = 1000L
private const val TIMEOUT_CONNECTED = 10_000L
private const val WAIT_WITH_NO_DATA = 100L
private const val DATA_BUFFER_SIZE = 1024
private const val TAG = "BluetoothConnectClassicImpl"

class BluetoothConnectClassicMonoImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothConnectClassicMono {

    private var connectionScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectionReceiveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()

    private var macAddressDeviceConnection: String? = null

    @Volatile
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    override fun connect(
        macAddressDevice: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        connectionScope.cancel()
        connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        connectionScope.launch {
            if (!mutex.tryLock()) {
                Log.e(TAG, "Already connecting...")
                withContext(Dispatchers.Main) { onError("Already connecting...") }
                return@launch
            }

            try {
                if (!bluetoothAdapter.isEnabled) {
                    withContext(Dispatchers.Main) {
                        onError(context.getString(io.github.msaggik.ui.R.string.no_check_bluetooth))
                    }
                    return@launch
                }

                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                macAddressDeviceConnection = macAddressDevice

                val device = bluetoothAdapter.getRemoteDevice(macAddressDevice)
                val tempSocket =
                    device.createRfcommSocketToServiceRecord(UUID.fromString(DEFAULT_UUID))

                withTimeoutOrNull(TIMEOUT_CONNECTED) {
                    if (bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                    tempSocket.connect()
                } ?: throw IOException("Connection timeout")

                socket = tempSocket
                inputStream = BufferedInputStream(socket?.inputStream)
                outputStream = socket?.outputStream

                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                disconnect()
                withContext(Dispatchers.Main) {
                    onError(e.message ?: context.getString(io.github.msaggik.ui.R.string.error_connection))
                }
            } finally {
                mutex.unlock()
            }
        }
    }

    override fun sendData(data: ByteArray) {
        connectionScope.launch {
            try {
                if (socket?.isConnected == true && socket?.outputStream != null) {
                    outputStream?.write(data)
                    outputStream?.flush()
                } else {
                    Log.e(TAG, "Attempting to send data when connection was broken")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error sending data: ${e.message}")
                disconnect()
            }
        }
    }

    override fun receiveData(onDataReceived: (String) -> Unit) {
        connectionScope.launch {
            connectionReceiveScope.cancel()
            connectionReceiveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            connectionReceiveScope.launch {
                val buffer = ByteArray(DATA_BUFFER_SIZE)
                val stringBuilder = StringBuilder()
                try {
                    while (isActuallyConnected()) {
                        if ((inputStream?.available() ?: 0) > 0) {
                            val bytesRead = inputStream?.read(buffer) ?: break
                            if (bytesRead > 0) {
                                val data = buffer.copyOf(bytesRead)
                                val jsonString = String(data, Charsets.UTF_8).trim()

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
                    Log.e(TAG, "Error receiving data: ${e.message}")
                    disconnect()
                }
            }
        }
    }

    override fun receiveDataClear() {
        connectionReceiveScope.cancel()
    }

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

    override fun disconnect() {
        connectionScope.launch {
            try {
                socket?.close()
                socket = null
                inputStream = null
                outputStream = null
                macAddressDeviceConnection = null
                Log.d(TAG, "Disconnected successfully")
            } catch (e: IOException) {
                Log.e(TAG, "Error closing socket: ${e.message}")
            }
        }
    }

    override fun isConnected(): Pair<Boolean, String?> =
        Pair(isActuallyConnected(), macAddressDeviceConnection)

    override fun connectedDevice(): Flow<String> = callbackFlow {
        val job = launch {
            while (isActive) {
                delay(INTERVAL_UPDATE_STATUS_DEVICES)
                val address =
                    if (isActuallyConnected()) macAddressDeviceConnection.orEmpty() else ""
                trySend(address).isSuccess
            }
        }
        awaitClose {
            job.cancel()
        }
    }

    private fun isActuallyConnected(): Boolean =
        socket?.isConnected == true && inputStream != null && outputStream != null
}