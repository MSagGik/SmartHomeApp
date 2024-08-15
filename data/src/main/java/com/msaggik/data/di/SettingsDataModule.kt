package com.msaggik.data.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import com.msaggik.data.api.bluetooth.BluetoothDevices
import android.bluetooth.BluetoothManager
import android.content.Context
import com.msaggik.data.api.bluetooth.impl.BluetoothDevicesImpl
import com.msaggik.data.api.sp.language.LanguageSharedPreferences
import com.msaggik.data.api.sp.language.impl.LanguageSharedPreferencesImpl
import com.msaggik.data.api.sp.theme.ThemeSharedPreferences
import com.msaggik.data.api.sp.theme.impl.ThemeSharedPreferencesImpl
import com.msaggik.data.repository_impl.bluetooth.BluetoothRepositoryImpl
import com.msaggik.data.repository_impl.language.LanguageRepositoryImpl
import com.msaggik.data.repository_impl.theme.ThemeRepositoryImpl
import com.msaggik.settings.domain.repository.BluetoothRepository
import com.msaggik.settings.domain.repository.LanguageRepository
import com.msaggik.settings.domain.repository.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val LIGHT_THEME_KEY = "light_theme_key"
private const val LANGUAGE_APP_KEY = "language_app_key"

val settingDataModule = module {

    // theme
    single<ThemeRepository> {
        ThemeRepositoryImpl(
            theme = get()
        )
    }

    single<ThemeSharedPreferences> {
        ThemeSharedPreferencesImpl(
            sp = androidContext().getSharedPreferences(LIGHT_THEME_KEY, Application.MODE_PRIVATE)
        )
    }

    // language
    single<LanguageRepository> {
        LanguageRepositoryImpl(
            language = get()
        )
    }

    single<LanguageSharedPreferences> {
        LanguageSharedPreferencesImpl(
            context = androidContext(),
            sp = androidContext().getSharedPreferences(LANGUAGE_APP_KEY, Application.MODE_PRIVATE)
        )
    }

    // bluetooth
    single<BluetoothRepository> {
        BluetoothRepositoryImpl(
            context = androidContext(),
            devices = get()
        )
    }

    single<BluetoothDevices> {
        BluetoothDevicesImpl(
            context = androidContext(),
            bluetoothAdapter = get()
        )
    }

    single<BluetoothAdapter> {
        (androidContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
}