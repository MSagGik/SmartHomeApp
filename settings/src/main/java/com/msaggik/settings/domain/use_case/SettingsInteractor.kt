package com.msaggik.settings.domain.use_case

import com.msaggik.settings.domain.model.ThemeApp

interface SettingsInteractor {
    suspend fun getTheme() : ThemeApp
    suspend fun updateTheme(theme: ThemeApp)
}