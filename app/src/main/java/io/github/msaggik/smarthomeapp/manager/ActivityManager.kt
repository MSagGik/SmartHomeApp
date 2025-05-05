package io.github.msaggik.smarthomeapp.manager

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import io.github.msaggik.settings.domain.use_case.SettingsInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ActivityManager(
    private val interactor: SettingsInteractor
) {

    fun initSettingApp() {
        val scope = CoroutineScope(SupervisorJob())
        scope.launch(Dispatchers.IO) {
            val isLightTheme = interactor.getTheme()
            val languageApp = interactor.getLanguageSharedPreferences()
            withContext(Dispatchers.Main) {
                setLanguage(languageApp.language)
                setApplicationTheme(isLightTheme.isLightTheme)
            }
        }
    }

    private fun setApplicationTheme(lightThemeEnabled: Boolean){
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (lightThemeEnabled) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES

        if (currentNightMode != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }

    private fun setLanguage(languageEnabled: String){
        if (languageEnabled != Locale.getDefault().toString().replace("_", "-")) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageEnabled)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}