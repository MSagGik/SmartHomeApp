package com.msaggik.data.api.sp.theme.impl

import android.content.SharedPreferences
import com.msaggik.data.api.sp.theme.ThemeSharedPreferences

private const val LIGHT_THEME_KEY = "light_theme_key"
class ThemeSharedPreferencesImpl(
    private val sp: SharedPreferences
) : ThemeSharedPreferences {

    override suspend fun isLightThemeSharedPreferences(): Boolean {
        return sp.getBoolean(LIGHT_THEME_KEY, true)
    }

    override suspend fun updateThemeSharedPreferences(isLightTheme: Boolean) {
        sp.edit().putBoolean(LIGHT_THEME_KEY, isLightTheme).apply()
    }
}