package io.github.msaggik.bluetooth.api.manager

import io.github.msaggik.bluetooth.api.entity.manager.BluetoothDevicesResponse

/**
 * Interface for managing Bluetooth Classic operations such as retrieving paired devices,
 * starting and stopping discovery of nearby devices.
 *
 * Implementations are expected to handle permission checks and lifecycle of discovery processes.
 */
interface BluetoothConnectManager {
    /**
     * Retrieves a list of Bluetooth devices that are already paired (bonded).
     *
     * @return A [BluetoothDevicesResponse] containing the paired devices and a result status code.
     *         Returns [BluetoothConnectState.ADAPTER_OFF] if Bluetooth is disabled,
     *         or [BluetoothConnectState.PERMISSION_OFF] if the required permission is missing.
     */
    fun getPairedBluetoothDevices(): BluetoothDevicesResponse

    /**
     * Starts discovery of nearby Bluetooth devices for a fixed duration.
     *
     * @param startSearchState Callback triggered when discovery starts.
     * @param finishSearchState Callback triggered when discovery completes after a delay.
     * @param errorSearchState Callback triggered if a [SecurityException] occurs during discovery.
     */
    fun searchBluetoothDevices(startSearchState: () -> Unit, finishSearchState: () -> Unit, errorSearchState: (String) -> Unit)

    /**
     * Cancels an ongoing Bluetooth discovery process.
     * Logs any [SecurityException] that occurs.
     */
    fun clearSearchBluetoothDevices()
}