package com.msaggik.data.di

import android.app.Application
import com.msaggik.data.api.sp.theme.ThemeSharedPreferences
import com.msaggik.data.api.sp.theme.impl.ThemeSharedPreferencesImpl
import com.msaggik.data.repository_impl.theme.ThemeRepositoryImpl
import com.msaggik.settings.domain.repository.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val LIGHT_THEME_KEY = "light_theme_key"

val settingDataModule = module {

    // data
    single<ThemeRepository> {
        ThemeRepositoryImpl(
            theme = get()
        )
    }

    single<ThemeSharedPreferences> {
        ThemeSharedPreferencesImpl(
            sp = get()
        )
    }

    single {
        androidContext()
            .getSharedPreferences(LIGHT_THEME_KEY, Application.MODE_PRIVATE)
    }
}