package io.github.msaggik.home.domain.model.device

sealed class LightingModeState {
    data class LightingModePolyState(
        val lightingModePoly: LightingModePoly
    ): LightingModeState()

    data class LightingModeMonoState(
        val lightingModeMono: LightingModeMono,
    ): LightingModeState()
}