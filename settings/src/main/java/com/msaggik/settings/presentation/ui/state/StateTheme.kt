package com.msaggik.settings.presentation.ui.state

import com.msaggik.common_ui.R

sealed class StateTheme(
    val stateImageTheme: Int,
    val stateText: Int
) {
    object Light : StateTheme(R.drawable.ic_theme, R.string.light_theme)
    object Dark : StateTheme(R.drawable.ic_theme_dark, R.string.dark_theme)
}