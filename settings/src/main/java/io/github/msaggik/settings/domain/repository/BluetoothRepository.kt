package io.github.msaggik.settings.domain.repository

import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.util.Resource
import kotlinx.coroutines.flow.Flow

interface BluetoothRepository {
    // manager bluetooth devices
    fun getBluetoothDevices() : Flow<Resource<List<SmartDevice>>>
    fun searchBluetoothDevices(startSearchState: () -> Unit, finishSearchState: () -> Unit, errorSearchState: (String) -> Unit)
    fun clearSearchBluetoothDevices()
    // service connected
    suspend fun connect(macAddress: String): Result<Unit>
    fun disconnect()
    fun isConnected(): Pair<Boolean, String?>
    fun sendData(data: ByteArray)
    fun receiveData(): Flow<String>
    fun observeConnectedDevice(): Flow<String>
}