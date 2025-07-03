package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.device.ColorModel

sealed class ColorsImageState {
    object Loading : ColorsImageState()

    class Content(
        val colors: List<ColorModel>
    ) : ColorsImageState()

    class Error(
        val errorMessage: String
    ) : ColorsImageState()

    object Empty : ColorsImageState()
}