package io.github.msaggik.home.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.util.Resource
import java.util.UUID

interface RepositoryBluetooth {
    // manager bluetooth devices
    fun getBluetoothDevices() : Flow<Resource<List<SmartDeviceRoom>>>

    // BluetoothConnectClassicPoly
    suspend fun connect(uuid: UUID, macAddressDevice: String): Result<Unit>
    fun disconnectDeviceViaMAC(macAddressDevice: String)
    fun disconnectAllDevice()

    fun sendData(macAddressDevice: String, data: ByteArray)
    fun sendListData(macAddressDevice: String, listData: List<ByteArray>)
    fun receiveData(macAddressDevice: String, onDataReceived: (String) -> Unit): Boolean
    fun receiveDataClear(macAddressDevice: String)

    fun isConnected(macAddressDevice: String): Pair<Boolean, String?>

    fun getConnectedDevices(): SharedFlow<List<String>>
    fun startDeviceConnectionUpdates()
    fun stopDeviceConnectionUpdates()
}