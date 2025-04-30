package io.github.msaggik.home.presentation.view_model.pager_view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import io.github.msaggik.home.domain.model.device.ClimateUIModel
import io.github.msaggik.home.domain.use_case.InteractorBluetoothClassic
import java.util.UUID

private const val TAG = "ClimateViewModel"

class ClimateViewModel(
    private val interactorBluetoothClassic: InteractorBluetoothClassic
) : ViewModel() {

    // bluetooth
    fun enablingDataReception(macAddressDevice: String, enabled: Boolean) {
        interactorBluetoothClassic.enablingDataReception(macAddressDevice = macAddressDevice, enabled = enabled)
    }

    private val _connectedDevices = MutableLiveData<List<String>>()
    val connectedDevices: LiveData<List<String>> = _connectedDevices

    private fun getConnectedDevices() {
        Log.i(TAG, "[getConnectedDevices] Subscribing to connected Bluetooth devices")
        interactorBluetoothClassic.getConnectedDevices()
            .onEach { listMacDevices ->
                Log.d(TAG, "[getConnectedDevices] Connected devices updated: $listMacDevices")
                _connectedDevices.postValue(listMacDevices)
            }.launchIn(viewModelScope)
    }

    private fun startDeviceConnectionUpdates() {
        Log.i(TAG, "[startDeviceConnectionUpdates] Starting device connection updates")
        interactorBluetoothClassic.startDeviceConnectionUpdates()
    }

    private fun stopDeviceConnectionUpdates() {
        Log.i(TAG, "[stopDeviceConnectionUpdates] Stopping device connection updates")
        interactorBluetoothClassic.stopDeviceConnectionUpdates()
    }

    fun connect(uuid: UUID, macAddress: String) {
        Log.i(TAG, "[connect] Connecting to device: $macAddress with UUID: $uuid")
        viewModelScope.launch {
            try {
                interactorBluetoothClassic.connect(
                    uuid = uuid,
                    macAddressDevice = macAddress
                )
                Log.d(TAG, "[connect] Connection initiated to $macAddress")
            } catch (e: Exception) {
                Log.e(TAG, " [connect]Connection failed to $macAddress: ${e.localizedMessage}", e)
            }
        }
    }

    fun disconnect(macAddress: String) {
        Log.i(TAG, "[disconnect] Disconnecting device: $macAddress")
        try {
            interactorBluetoothClassic.disconnectDeviceViaMAC(macAddress)
            Log.d(TAG, "[disconnect] Device disconnected: $macAddress")
        } catch (e: Exception) {
            Log.e(TAG, "[disconnect] Failed to disconnect $macAddress: ${e.localizedMessage}", e)
        }
    }

    private val _climateUiState = MutableStateFlow<ClimateUIModel>(ClimateUIModel.defaultClimateUIModel())
    val climateUiState: StateFlow<ClimateUIModel> = _climateUiState

    fun receiveData(macAddress: String): Boolean {
        Log.i(TAG, "[receiveData] Receiving climate data from $macAddress")
        val deviceFlow = interactorBluetoothClassic.getDeviceFlow(macAddress) ?: return  false
        viewModelScope.launch {
            deviceFlow.collect { rawJson ->
                Log.v(TAG, "[receiveData] Raw data received: $rawJson")
                val parsed = ClimateUIModel.climateDataConvert(rawJson)
                parsed?.let {
                    viewModelScope.launch {
                        _climateUiState.value = it
                    }
                }
            }
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "[onCleared] ViewModel cleared, stopping connection updates")
        stopDeviceConnectionUpdates()
    }

    init {
        Log.i(TAG, "[init] ClimateViewModel initialized")
        getConnectedDevices()
        startDeviceConnectionUpdates()
    }
}