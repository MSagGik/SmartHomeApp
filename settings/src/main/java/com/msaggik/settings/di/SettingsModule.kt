package com.msaggik.settings.di

import com.msaggik.settings.domain.use_case.SettingsInteractor
import com.msaggik.settings.domain.use_case.impl.SettingsInteractorImpl
import com.msaggik.settings.presentation.view_model.DevicesViewModel
import com.msaggik.settings.presentation.view_model.SettingsViewModel
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
    single<SettingsInteractor> {
        SettingsInteractorImpl(
            repositoryTheme = get(),
            repositoryLanguage = get(),
            repositoryBluetooth = get()
        )
    }
}