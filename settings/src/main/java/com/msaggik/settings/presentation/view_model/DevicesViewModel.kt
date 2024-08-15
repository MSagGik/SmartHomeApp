package com.msaggik.settings.presentation.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaggik.settings.domain.model.SmartDevice
import com.msaggik.settings.domain.use_case.SettingsInteractor
import com.msaggik.settings.presentation.ui.state.DeviceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DevicesViewModel (
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<DeviceState>()

    fun getStateLiveData(): LiveData<DeviceState> = mediatorStateLiveData

    private val mediatorStateLiveData = MediatorLiveData<DeviceState>().also { liveData ->
        liveData.addSource(stateLiveData) { state ->
            liveData.value = when (state) {
                is DeviceState.Content -> DeviceState.Content(state.devices)
                is DeviceState.Empty -> state
                is DeviceState.Error -> state
                is DeviceState.Loading -> state
            }
        }
    }

    fun getDevices() {
        renderState(DeviceState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            settingsInteractor
                .getBluetoothDevices()
                .collect { pair -> devicesResult(pair.first, pair.second) }
        }

    }

    private fun devicesResult(foundDevices: List<SmartDevice>?, errorMessage: String?) {
        val devices = mutableListOf<SmartDevice>()
        if (foundDevices != null) {
            devices.addAll(foundDevices)
        }

        when {
            errorMessage != null -> {
                renderState(DeviceState.Error(errorMessage = errorMessage))
            }

            devices.isEmpty() -> {
                renderState(DeviceState.Empty)
            }

            else -> {
                renderState(DeviceState.Content(devices = devices ))
            }
        }
    }

    private fun renderState(state: DeviceState) {
        stateLiveData.postValue(state)
    }
}