package com.msaggik.data.api.sp.theme

interface ThemeSharedPreferences {
    suspend fun isLightThemeSharedPreferences() : Boolean
    suspend fun updateThemeSharedPreferences(isLightTheme : Boolean)
}