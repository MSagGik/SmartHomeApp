package com.msaggik.smarthomeapp.root

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.msaggik.data.di.settingDataModule
import com.msaggik.settings.di.settingModule
import com.msaggik.settings.domain.use_case.SettingsInteractor
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

        val provideThemeInteractor: SettingsInteractor by inject()

        MainScope().launch (Dispatchers.IO){
            val theme = provideThemeInteractor.getTheme()
            AppCompatDelegate.setDefaultNightMode(
                if(theme.isLightTheme) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }
    }
}