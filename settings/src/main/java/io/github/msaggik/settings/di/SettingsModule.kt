package io.github.msaggik.settings.di

import io.github.msaggik.settings.data.repositoryimpl.bluetooth.BluetoothRepositoryImpl
import io.github.msaggik.settings.data.repositoryimpl.language.LanguageRepositoryImpl
import io.github.msaggik.settings.data.repositoryimpl.theme.ThemeRepositoryImpl
import io.github.msaggik.settings.domain.repository.BluetoothRepository
import io.github.msaggik.settings.domain.repository.LanguageRepository
import io.github.msaggik.settings.domain.repository.ThemeRepository
import io.github.msaggik.settings.domain.use_case.SettingsInteractor
import io.github.msaggik.settings.domain.use_case.impl.SettingsInteractorImpl
import io.github.msaggik.settings.presentation.view_model.DevicesViewModel
import io.github.msaggik.settings.presentation.view_model.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingModule = module {

    // view-model
    viewModel{
        SettingsViewModel(
            settingsInteractor = get()
        )
    }

    viewModel{
        DevicesViewModel(
            settingsInteractor = get()
        )
    }

    // domain
    factory<SettingsInteractor> {
        SettingsInteractorImpl(
            repositoryTheme = get(),
            repositoryLanguage = get(),
            repositoryBluetooth = get()
        )
    }

    // repository
    // theme
    factory<ThemeRepository> {
        ThemeRepositoryImpl(
            theme = get()
        )
    }

    // language
    factory<LanguageRepository> {
        LanguageRepositoryImpl(
            language = get()
        )
    }

    // bluetooth
    factory<BluetoothRepository> {
        BluetoothRepositoryImpl(
            context = androidContext(),
            bluetoothConnectManager = get(),
            bluetoothConnectClassicMono = get()
        )
    }
}