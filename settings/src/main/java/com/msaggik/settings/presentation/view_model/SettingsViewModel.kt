package com.msaggik.settings.presentation.view_model

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.use_case.SettingsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel (
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val themeLiveData = MutableLiveData<Boolean>()
    private var isLightTheme = true

    init {
        viewModelScope.launch (Dispatchers.IO) {
            isLightTheme = settingsInteractor.getTheme().isLightTheme
        }
        setApplicationTheme(isLightTheme)
        themeLiveData.postValue(isLightTheme)
    }

    fun getTheme(): LiveData<Boolean> = themeLiveData

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
}