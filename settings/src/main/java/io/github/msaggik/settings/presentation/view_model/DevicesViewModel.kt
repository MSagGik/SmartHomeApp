package io.github.msaggik.settings.presentation.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.use_case.SettingsInteractor
import io.github.msaggik.settings.presentation.view_model.state.DeviceSavedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import io.github.msaggik.settings.presentation.view_model.appdata.AppDataSettings
import io.github.msaggik.settings.presentation.view_model.state.DeviceSearchedState

class DevicesViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {
    // saved bluetooth device
    val deviceListSaved: MutableList<SmartDevice> = mutableListOf()

    private val stateSavedBluetoothDeviceLiveData = MutableLiveData<DeviceSavedState>()
    fun getStateSavedBluetoothDeviceLiveData(): LiveData<DeviceSavedState> = stateSavedBluetoothDeviceLiveData

    fun getSavedDevices() {
        stateSavedBluetoothDeviceLiveData.postValue(DeviceSavedState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            settingsInteractor
                .getBluetoothDevices()
                .collect { pair -> devicesResult(pair.first, pair.second) }
        }
    }

    private fun devicesResult(foundDevices: List<SmartDevice>?, errorMessage: String?) {
        when {
            errorMessage != null -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceSavedState.Error(errorMessage = errorMessage))
            }
            !foundDevices.isNullOrEmpty() -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceSavedState.Content(devices = foundDevices))
            }
            else -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceSavedState.Empty)
            }
        }
    }

    // searched bluetooth device
    val deviceListSearched: MutableList<SmartDevice> = mutableListOf()

    private val stateSearchedBluetoothDeviceLiveData = MutableLiveData<DeviceSearchedState>()
    fun getStateSearchedBluetoothDeviceLiveData(): LiveData<DeviceSearchedState> = stateSearchedBluetoothDeviceLiveData

    fun searchBluetoothDevices() {
        settingsInteractor.searchBluetoothDevices(
            startSearchState = {
                stateSearchedBluetoothDeviceLiveData.postValue(DeviceSearchedState.Loading)
            },
            finishSearchState = {
                stateSearchedBluetoothDeviceLiveData.postValue(DeviceSearchedState.FinishSearched)
            },
            errorSearchState = { errorMessage ->
                stateSearchedBluetoothDeviceLiveData.postValue(DeviceSearchedState.Error(errorMessage))
            }
        )
    }

    // observe connected devices
    private val _observeConnectedDevice = MutableStateFlow("")
    val observeConnectedDevice: StateFlow<String> = _observeConnectedDevice

    fun startObserveConnectedDevice() {
        viewModelScope.launch {
            settingsInteractor.observeConnectedDevice().collect { address ->
                _observeConnectedDevice.value = address
            }
        }
    }

    // received data
    private val _receivedData = MutableSharedFlow<String>()
    val receivedData: SharedFlow<String> = _receivedData.asSharedFlow()

    fun receiveData() {
        viewModelScope.launch {
            settingsInteractor.receiveData().collect { data ->
                _receivedData.emit(data)
            }
        }
    }

    // cleanup of resources when fragment is destroyed
    fun clearSearchBluetoothDevices() {
        settingsInteractor.clearSearchBluetoothDevices()
    }

    // establishing a connection with a bluetooth device
    fun connect(macAddress: String) {
        viewModelScope.launch {
            settingsInteractor.connect(macAddress)
        }
    }

    fun disconnect() {
        settingsInteractor.disconnect()
    }

    fun isConnected(): Pair<Boolean, String?> {
        return settingsInteractor.isConnected()
    }

    // send data
    fun sendMessage(message: String) {
        viewModelScope.launch {
            settingsInteractor.sendData(message.toByteArray())
        }
    }

    override fun onCleared() {
        super.onCleared()
        AppDataSettings.deviceListSaved.clear()
        AppDataSettings.deviceListSearched.clear()
        AppDataSettings.deviceListSaved.addAll(deviceListSaved)
        AppDataSettings.deviceListSearched.addAll(deviceListSearched)
    }

    init {
        deviceListSaved.addAll(AppDataSettings.deviceListSaved)
        deviceListSearched.addAll(AppDataSettings.deviceListSearched)
    }
}