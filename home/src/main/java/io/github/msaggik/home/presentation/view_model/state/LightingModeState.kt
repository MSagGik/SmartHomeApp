package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.device.LightingModePoly

sealed class LightingModeState {
    object Loading : LightingModeState()

    class Content(
        val lightingModePoly: LightingModePoly
    ) : LightingModeState()

    class Error(
        val errorMessage: String
    ) : LightingModeState()
}