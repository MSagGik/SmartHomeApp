package io.github.msaggik.settings.presentation.view_model.state

import io.github.msaggik.settings.domain.model.SmartDevice

sealed interface DeviceSavedState {

    object Loading : DeviceSavedState

    data class Content(
        val devices: List<SmartDevice>
    ) : DeviceSavedState

    data class Error(
        val errorMessage: String
    ) : DeviceSavedState

    object  Empty : DeviceSavedState
}