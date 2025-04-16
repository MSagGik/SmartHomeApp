package io.github.msaggik.bluetooth.api.manager.impl

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.msaggik.bluetooth.api.entity.manager.state.BluetoothConnectState
import io.github.msaggik.bluetooth.api.entity.manager.BluetoothDevicesList
import io.github.msaggik.bluetooth.api.entity.manager.BluetoothDevicesResponse
import io.github.msaggik.bluetooth.api.manager.BluetoothConnectManager

private const val TAG = "Lib BluetoothManager"
private const val INTERVAL_SEARCH_DEVICES = 5000L

/**
 * Implementation of [BluetoothConnectManager] that manages Bluetooth Classic device interactions,
 * including paired device retrieval and discovery scanning.
 *
 * @param context The application context used for permission checks.
 * @param bluetoothAdapter The [BluetoothAdapter] instance used for Bluetooth operations.
 */
class BluetoothConnectManagerImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothConnectManager {

    private val connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Retrieves a list of Bluetooth devices that are already paired (bonded).
     *
     * @return A [BluetoothDevicesResponse] containing the paired devices and a result status code.
     *         Returns [BluetoothConnectState.ADAPTER_OFF] if Bluetooth is disabled,
     *         or [BluetoothConnectState.PERMISSION_OFF] if the required permission is missing.
     */
    override fun getPairedBluetoothDevices(): BluetoothDevicesResponse {
        Log.i(TAG, "[getPairedBluetoothDevices] start")
        if (!bluetoothAdapter.isEnabled) {
            return BluetoothDevicesResponse().apply {
                resultCode = BluetoothConnectState.ADAPTER_OFF
            }
        } else if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return BluetoothDevicesResponse().apply {
                resultCode = BluetoothConnectState.PERMISSION_OFF
            }
        }
        val bluetoothDevices = bluetoothAdapter.bondedDevices.toList()
        return BluetoothDevicesList(bluetoothDevices).apply {
            resultCode = BluetoothConnectState.SUCCESS
        }
    }

    /**
     * Starts discovery of nearby Bluetooth devices for a fixed duration.
     *
     * @param startSearchState Callback triggered when discovery starts.
     * @param finishSearchState Callback triggered when discovery completes after a delay.
     * @param errorSearchState Callback triggered if a [SecurityException] occurs during discovery.
     */
    override fun searchBluetoothDevices(
        startSearchState: () -> Unit,
        finishSearchState: () -> Unit,
        errorSearchState: (String) -> Unit,
    ) {
        Log.i(TAG, "[searchBluetoothDevices] start")
        connectionScope.launch {
            try {
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                bluetoothAdapter.startDiscovery()
                withContext(Dispatchers.Main) { startSearchState() }
                withContext(Dispatchers.IO) {
                    delay(INTERVAL_SEARCH_DEVICES)
                    withContext(Dispatchers.Main) {
                        finishSearchState()
                        bluetoothAdapter.cancelDiscovery()
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "[searchBluetoothDevices] ${e.message.toString()}")
                withContext(Dispatchers.Main) {
                    errorSearchState(
                        e.message ?: context.getString(io.github.msaggik.ui.R.string.error_message)
                    )
                }
            }
        }
    }

    /**
     * Cancels an ongoing Bluetooth discovery process.
     * Logs any [SecurityException] that occurs.
     */
    override fun clearSearchBluetoothDevices() {
        Log.i(TAG, "[clearSearchBluetoothDevices] start")
        try {
            bluetoothAdapter.cancelDiscovery()
        } catch (e: SecurityException) {
            Log.e(TAG, "[clearSearchBluetoothDevices] ${e.message.toString()}")
        }
    }
}