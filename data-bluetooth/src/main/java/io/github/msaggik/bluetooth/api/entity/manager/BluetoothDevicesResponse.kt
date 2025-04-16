package io.github.msaggik.bluetooth.api.entity.manager

import io.github.msaggik.bluetooth.api.entity.manager.state.BluetoothConnectState

/**
 * Base class representing the result of a Bluetooth device query operation.
 *
 * @property resultCode A [BluetoothConnectState] enum indicating the status of the operation.
 */
open class BluetoothDevicesResponse {
    var resultCode: BluetoothConnectState = BluetoothConnectState.DEFAULT
}