package io.github.msaggik.bluetooth.api.classic_mono

import kotlinx.coroutines.flow.Flow

interface BluetoothConnectClassicMono {
    fun connect(macAddressDevice: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun sendData(data: ByteArray)
    fun receiveData(onDataReceived: (String) -> Unit)
    fun receiveDataClear()
    fun disconnect()
    fun isConnected(): Pair<Boolean, String?>
    fun connectedDevice(): Flow<String>
}