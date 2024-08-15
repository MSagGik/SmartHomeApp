package com.msaggik.settings.domain.repository

import com.msaggik.settings.domain.model.ThemeApp

interface ThemeRepository {
    suspend fun isLightTheme() : ThemeApp
    suspend fun updateTheme(isLightTheme : ThemeApp)
}