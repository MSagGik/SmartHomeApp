package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.device.SmartDeviceRoom

sealed interface DeviceRoomSavedState {

    object Loading : DeviceRoomSavedState

    data class Content(
        val devices: List<SmartDeviceRoom>
    ) : DeviceRoomSavedState

    data class Error(
        val errorMessage: String
    ) : DeviceRoomSavedState

    object  Empty : DeviceRoomSavedState
}