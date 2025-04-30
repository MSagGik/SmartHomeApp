package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.device.LightingModePoly

sealed class LightingModesState {
    object Loading : LightingModesState()

    class Content(
        val lightingModePolies: List<LightingModePoly>
    ) : LightingModesState()

    class Error(
        val errorMessage: String
    ) : LightingModesState()
}