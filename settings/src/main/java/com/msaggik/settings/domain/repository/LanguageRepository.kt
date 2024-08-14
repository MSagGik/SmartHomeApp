package com.msaggik.settings.domain.repository

import com.msaggik.settings.domain.model.LanguageApp

interface LanguageRepository {
    suspend fun getLanguageSharedPreferences() : LanguageApp
    suspend fun setLanguage(languageAdd: LanguageApp)
    suspend fun setLanguageApplication(languageAdd: LanguageApp)
}