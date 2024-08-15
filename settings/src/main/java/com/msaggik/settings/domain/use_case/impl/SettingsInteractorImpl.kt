package com.msaggik.settings.domain.use_case.impl

import com.msaggik.common_util.Resource
import com.msaggik.settings.domain.model.LanguageApp
import com.msaggik.settings.domain.model.SmartDevice
import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.repository.BluetoothRepository
import com.msaggik.settings.domain.repository.LanguageRepository
import com.msaggik.settings.domain.repository.ThemeRepository
import com.msaggik.settings.domain.use_case.SettingsInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsInteractorImpl (
    private val repositoryTheme: ThemeRepository,
    private val repositoryLanguage: LanguageRepository,
    private val repositoryBluetooth: BluetoothRepository,
) : SettingsInteractor {

    override suspend fun getTheme(): ThemeApp {
        return repositoryTheme.isLightTheme()
    }

    override suspend fun updateTheme(theme: ThemeApp) {
        repositoryTheme.updateTheme(theme)
    }

    override suspend fun getLanguageSharedPreferences(): LanguageApp {
        return repositoryLanguage.getLanguageSharedPreferences()
    }

    override suspend fun setLanguage(languageAdd: LanguageApp) {
        repositoryLanguage.setLanguage(languageAdd)
    }

    override suspend fun setLanguageApplication(languageAdd: LanguageApp) {
        repositoryLanguage.setLanguageApplication(languageAdd)
    }

    override fun getBluetoothDevices(): Flow<Pair<List<SmartDevice>?, String?>> {
        return repositoryBluetooth.getBluetoothDevices().map { resource -> Resource.handleResource(resource) }
    }
}