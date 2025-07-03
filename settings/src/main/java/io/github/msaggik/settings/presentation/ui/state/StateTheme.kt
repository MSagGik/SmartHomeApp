package io.github.msaggik.settings.presentation.ui.state

sealed class StateTheme(
    val stateImageTheme: Int,
    val stateText: Int
) {
    object Light : StateTheme(io.github.msaggik.ui.R.drawable.ic_theme, io.github.msaggik.ui.R.string.light_theme)
    object Dark : StateTheme(io.github.msaggik.ui.R.drawable.ic_theme_dark, io.github.msaggik.ui.R.string.dark_theme)
}