package com.msaggik.settings.domain.use_case.impl

import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.repository.ThemeRepository
import com.msaggik.settings.domain.use_case.SettingsInteractor

class SettingsInteractorImpl (
    private val repository: ThemeRepository
) : SettingsInteractor {

    override suspend fun getTheme(): ThemeApp {
        return repository.isLightTheme()
    }

    override suspend fun updateTheme(theme: ThemeApp) {
        repository.updateTheme(theme)
    }
}