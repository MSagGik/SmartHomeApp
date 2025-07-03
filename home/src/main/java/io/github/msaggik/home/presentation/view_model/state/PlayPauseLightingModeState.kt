package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.R

sealed class PlayPauseLightingModeState(val mode: Long, val stateView: Int) {
    data object PlayOff : PlayPauseLightingModeState(
        mode = 0L,
        stateView = R.drawable.ic_play_off
    )
    data class Play(val idLightingMode: Long) : PlayPauseLightingModeState(
        mode = idLightingMode,
        stateView = R.drawable.ic_play
    )
    data class Pause(val idLightingMode: Long) : PlayPauseLightingModeState(
        mode = idLightingMode,
        stateView = R.drawable.ic_pause
    )
}