package com.msaggik.settings.presentation.view_model

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaggik.settings.domain.model.LanguageApp
import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.use_case.SettingsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel (
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val themeLiveData = MutableLiveData<Boolean>()
    private var isLightTheme = true
    private val languageLiveData = MutableLiveData<String>()
    private var language = ""

    init {
        viewModelScope.launch (Dispatchers.IO) {
            isLightTheme = settingsInteractor.getTheme().isLightTheme
            themeLiveData.postValue(isLightTheme)
        }
        viewModelScope.launch (Dispatchers.IO) {
            val languageApp = settingsInteractor.getLanguageSharedPreferences()
            language = languageApp.language
            languageLiveData.postValue(language)
        }
    }

    fun switchTheme(lightThemeEnabled: Boolean) {
        isLightTheme = lightThemeEnabled
        viewModelScope.launch (Dispatchers.IO) {
            settingsInteractor.updateTheme(ThemeApp(isLightTheme))
        }
        setApplicationTheme(isLightTheme)
        themeLiveData.postValue(isLightTheme)
    }

    private fun setApplicationTheme(lightThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if(lightThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }

    fun getLanguage(): LiveData<String> = languageLiveData

    fun setLanguage(languageEnabled: String) {
        language = languageEnabled
        viewModelScope.launch (Dispatchers.IO) {
            settingsInteractor.setLanguage(LanguageApp(language))
        }
        languageLiveData.postValue(language)
    }
}