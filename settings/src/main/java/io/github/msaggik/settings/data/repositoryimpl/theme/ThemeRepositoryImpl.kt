package io.github.msaggik.settings.data.repositoryimpl.theme

import io.github.msaggik.sp.api.theme.ThemeSharedPreferences
import io.github.msaggik.settings.domain.model.ThemeApp
import io.github.msaggik.settings.domain.repository.ThemeRepository

class ThemeRepositoryImpl (
    private val theme: ThemeSharedPreferences
) : ThemeRepository {

    override suspend fun isLightTheme(): ThemeApp {
        return ThemeApp(
            theme.isLightThemeSharedPreferences()
        )
    }

    override suspend fun updateTheme(isLightTheme: ThemeApp) {
        theme.updateThemeSharedPreferences(isLightTheme.isLightTheme)
    }
}