package io.github.msaggik.settings.presentation.view_model.state

sealed interface DeviceSearchedState {

    object Loading : DeviceSearchedState

    object FinishSearched : DeviceSearchedState

    data class Error(
        val errorMessage: String
    ) : DeviceSearchedState
}