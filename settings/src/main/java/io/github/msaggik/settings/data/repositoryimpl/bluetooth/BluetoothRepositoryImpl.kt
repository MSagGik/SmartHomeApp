package io.github.msaggik.settings.data.repositoryimpl.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import io.github.msaggik.bluetooth.api.classic_mono.BluetoothConnectClassicMono
import io.github.msaggik.bluetooth.api.entity.manager.BluetoothDevicesList
import io.github.msaggik.bluetooth.api.entity.manager.state.BluetoothConnectState
import io.github.msaggik.bluetooth.api.manager.BluetoothConnectManager
import kotlinx.coroutines.channels.awaitClose
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import io.github.msaggik.settings.data.mapers.SettingMappers
import io.github.msaggik.util.Resource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "BluetoothRepositoryImpl"

class BluetoothRepositoryImpl(
    private val context: Context,
    private val bluetoothConnectManager: BluetoothConnectManager,
    private val bluetoothConnectClassicMono: BluetoothConnectClassicMono
) : BluetoothRepository {

    @SuppressLint("MissingPermission")
    override fun getBluetoothDevices(): Flow<Resource<List<SmartDevice>>> = flow {
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
                    SettingMappers.map(it)
                }))
            }

            BluetoothConnectState.DEFAULT -> {
                emit(Resource.Error(context.getString(io.github.msaggik.ui.R.string.error_message)))
            }
        }
    }

    override fun searchBluetoothDevices(
        startSearchState: () -> Unit,
        finishSearchState: () -> Unit,
        errorSearchState: (String) -> Unit
    ) {
        bluetoothConnectManager.searchBluetoothDevices(
            startSearchState = startSearchState,
            finishSearchState = finishSearchState,
            errorSearchState = errorSearchState
        )
    }

    override fun clearSearchBluetoothDevices() {
        bluetoothConnectManager.clearSearchBluetoothDevices()
    }

    override suspend fun connect(macAddress: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            bluetoothConnectClassicMono.connect(macAddress,
                onSuccess = { continuation.resume(Result.success(Unit)) },
                onError = { error -> continuation.resume(Result.failure(Exception(error))) }
            )
        }
    }

    override fun disconnect() {
        bluetoothConnectClassicMono.disconnect()
    }

    override fun isConnected(): Pair<Boolean, String?> = bluetoothConnectClassicMono.isConnected()

    override fun sendData(data: ByteArray) {
        bluetoothConnectClassicMono.sendData(data)
    }

    override fun receiveData(): Flow<String> = callbackFlow {
        bluetoothConnectClassicMono.receiveData { json ->
            trySend(json).isSuccess
        }
        awaitClose {
            bluetoothConnectClassicMono.receiveDataClear()
        }
    }

    override fun observeConnectedDevice(): Flow<String> {
        return bluetoothConnectClassicMono.connectedDevice()
    }
}