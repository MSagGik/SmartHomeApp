package io.github.msaggik.home.data.repositoryimpl.devices

import android.annotation.SuppressLint
import android.content.Context
import io.github.msaggik.bluetooth.api.classic_poly.BluetoothConnectClassicPoly
import io.github.msaggik.bluetooth.api.entity.manager.BluetoothDevicesList
import io.github.msaggik.bluetooth.api.entity.manager.state.BluetoothConnectState
import io.github.msaggik.bluetooth.api.manager.BluetoothConnectManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import io.github.msaggik.home.data.mappers.HomeMappers
import io.github.msaggik.util.Resource
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.repository.RepositoryBluetooth
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RepositoryBluetoothImpl(
    private val context: Context,
    private val bluetoothConnectManager: BluetoothConnectManager,
    private val bluetoothConnectClassic: BluetoothConnectClassicPoly,
) : RepositoryBluetooth {

    @SuppressLint("MissingPermission")
    override fun getBluetoothDevices(): Flow<Resource<List<SmartDeviceRoom>>> = flow {
        val response = bluetoothConnectManager.getPairedBluetoothDevices()
        when (response.resultCode) {
            BluetoothConnectState.ADAPTER_OFF -> {
                emit(Resource.Error(context.getString(io.github.msaggik.ui.R.string.no_check_bluetooth)))
            }

            BluetoothConnectState.PERMISSION_OFF -> {
                emit(Resource.Error(context.getString(io.github.msaggik.ui.R.string.no_permission)))
            }

            BluetoothConnectState.SUCCESS -> {
                emit(Resource.Success((response as BluetoothDevicesList).list.map {
                    HomeMappers.map(it)
                }))
            }

            BluetoothConnectState.DEFAULT -> {
                emit(Resource.Error(context.getString(io.github.msaggik.ui.R.string.error_message)))
            }
        }
    }

    override suspend fun connect(
        uuid: UUID,
        macAddressDevice: String
    ): Result<Unit> {
        return suspendCoroutine { continuation ->
            bluetoothConnectClassic.connect(
                uuid = uuid,
                macAddressDevice = macAddressDevice,
                onSuccess = { continuation.resume(Result.success(Unit)) },
                onError = { error -> continuation.resume(Result.failure(Exception(error))) }
            )
        }
    }

    override fun disconnectDeviceViaMAC(macAddressDevice: String) {
        bluetoothConnectClassic.disconnectDeviceViaMAC(macAddressDevice)
    }

    override fun disconnectAllDevice() {
        bluetoothConnectClassic.disconnectAllDevice()
    }

    override fun sendData(macAddressDevice: String, data: ByteArray) {
        bluetoothConnectClassic.sendData(
            macAddressDevice = macAddressDevice,
            data = data
        )
    }

    override fun sendListData(macAddressDevice: String, listData: List<ByteArray>) {
        bluetoothConnectClassic.sendListData(
            macAddressDevice = macAddressDevice,
            listData = listData
        )
    }

    override fun receiveData(macAddressDevice: String, onDataReceived: (String) -> Unit): Boolean {
        return bluetoothConnectClassic.receiveData(
            macAddressDevice = macAddressDevice,
            onDataReceived = onDataReceived
        )
    }

    override fun receiveDataClear(macAddressDevice: String) {
        bluetoothConnectClassic.receiveDataClear(macAddressDevice)
    }

    override fun isConnected(macAddressDevice: String): Pair<Boolean, String?> = bluetoothConnectClassic.isConnected(macAddressDevice)

    override fun getConnectedDevices(): SharedFlow<List<String>> {
        return bluetoothConnectClassic.getConnectedDevices()
    }

    override fun startDeviceConnectionUpdates() {
        bluetoothConnectClassic.startDeviceConnectionUpdates()
    }

    override fun stopDeviceConnectionUpdates() {
        bluetoothConnectClassic.stopDeviceConnectionUpdates()
    }
}