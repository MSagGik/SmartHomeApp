package com.msaggik.data.repository_impl.theme

import com.msaggik.data.api.sp.theme.ThemeSharedPreferences
import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.repository.ThemeRepository

class ThemeRepositoryImpl (
    private val theme: ThemeSharedPreferences
) : ThemeRepository{

    override suspend fun isLightTheme(): ThemeApp {
        return ThemeApp(
            theme.isLightThemeSharedPreferences()
        )
    }

    override suspend fun updateTheme(isLightTheme: ThemeApp) {
        theme.updateThemeSharedPreferences(isLightTheme.isLightTheme)
    }
}