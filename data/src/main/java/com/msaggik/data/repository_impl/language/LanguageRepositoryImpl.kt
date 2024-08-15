package com.msaggik.data.repository_impl.language

import com.msaggik.data.api.sp.language.LanguageSharedPreferences
import com.msaggik.settings.domain.model.LanguageApp
import com.msaggik.settings.domain.repository.LanguageRepository

class LanguageRepositoryImpl (
    private val language: LanguageSharedPreferences
) : LanguageRepository {

    override suspend fun getLanguageSharedPreferences(): LanguageApp {
        return LanguageApp(
            language.getLanguageSharedPreferences()
        )
    }

    override suspend fun setLanguage(languageAdd: LanguageApp) {
        language.setLanguage(languageAdd.language)
    }

    override suspend fun setLanguageApplication(languageAdd: LanguageApp) {
        language.setLanguageApplication(languageAdd.language)
    }
}