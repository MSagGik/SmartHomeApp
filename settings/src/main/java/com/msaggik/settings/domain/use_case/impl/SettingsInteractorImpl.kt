package com.msaggik.settings.domain.use_case.impl

import com.msaggik.settings.domain.model.LanguageApp
import com.msaggik.settings.domain.model.ThemeApp
import com.msaggik.settings.domain.repository.LanguageRepository
import com.msaggik.settings.domain.repository.ThemeRepository
import com.msaggik.settings.domain.use_case.SettingsInteractor

class SettingsInteractorImpl (
    private val repositoryTheme: ThemeRepository,
    private val repositoryLanguage: LanguageRepository
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
}