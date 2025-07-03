package io.github.msaggik.settings.data.repositoryimpl.language

import io.github.msaggik.sp.api.language.LanguageSharedPreferences
import io.github.msaggik.settings.domain.model.LanguageApp
import io.github.msaggik.settings.domain.repository.LanguageRepository

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
}