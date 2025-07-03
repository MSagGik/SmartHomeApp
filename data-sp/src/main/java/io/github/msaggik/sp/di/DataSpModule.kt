package io.github.msaggik.sp.di

import android.app.Application
import io.github.msaggik.sp.api.language.LanguageSharedPreferences
import io.github.msaggik.sp.api.language.impl.LanguageSharedPreferencesImpl
import io.github.msaggik.sp.api.theme.ThemeSharedPreferences
import io.github.msaggik.sp.api.theme.impl.ThemeSharedPreferencesImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val LIGHT_THEME_KEY = "light_theme_key"
private const val LANGUAGE_APP_KEY = "language_app_key"

val dataSpModule = module {

    // theme
    single<ThemeSharedPreferences> {
        ThemeSharedPreferencesImpl(
            sp = androidContext().getSharedPreferences(LIGHT_THEME_KEY, Application.MODE_PRIVATE)
        )
    }

    // language
    single<LanguageSharedPreferences> {
        LanguageSharedPreferencesImpl(
            context = androidContext(),
            sp = androidContext().getSharedPreferences(LANGUAGE_APP_KEY, Application.MODE_PRIVATE)
        )
    }
}