package io.github.msaggik.sp.api.theme

interface ThemeSharedPreferences {
    suspend fun isLightThemeSharedPreferences() : Boolean
    suspend fun updateThemeSharedPreferences(isLightTheme : Boolean)
}