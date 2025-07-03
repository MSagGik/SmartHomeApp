package io.github.msaggik.settings.domain.repository

import io.github.msaggik.settings.domain.model.ThemeApp

interface ThemeRepository {
    suspend fun isLightTheme() : ThemeApp
    suspend fun updateTheme(isLightTheme : ThemeApp)
}