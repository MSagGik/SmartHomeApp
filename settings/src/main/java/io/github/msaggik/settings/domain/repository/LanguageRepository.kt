package io.github.msaggik.settings.domain.repository

import io.github.msaggik.settings.domain.model.LanguageApp

interface LanguageRepository {
    suspend fun getLanguageSharedPreferences() : LanguageApp
    suspend fun setLanguage(languageAdd: LanguageApp)
}