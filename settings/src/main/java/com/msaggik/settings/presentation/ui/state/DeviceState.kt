package com.msaggik.settings.presentation.ui.state

import com.msaggik.settings.domain.model.SmartDevice

sealed interface DeviceState {

    object Loading : DeviceState

    data class Content(
        val devices: List<SmartDevice>
    ) : DeviceState

    data class Error(
        val errorMessage: String
    ) : DeviceState

    object  Empty : DeviceState
}