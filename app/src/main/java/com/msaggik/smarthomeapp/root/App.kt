package com.msaggik.smarthomeapp.root

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.msaggik.data.di.settingDataModule
import com.msaggik.settings.di.settingModule
import com.msaggik.settings.domain.use_case.SettingsInteractor
import com.msaggik.common_ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin


class App : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                settingDataModule,
                settingModule
            )
        }

        val settingsInteractor: SettingsInteractor by inject()

        MainScope().launch (Dispatchers.IO){
            val theme = settingsInteractor.getTheme()
            AppCompatDelegate.setDefaultNightMode(
                if(theme.isLightTheme) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
            )
            val language = settingsInteractor.getLanguageSharedPreferences()
            if(language.language != getString(R.string.default_)) {
                settingsInteractor.setLanguageApplication(language)
            }
        }
    }
}