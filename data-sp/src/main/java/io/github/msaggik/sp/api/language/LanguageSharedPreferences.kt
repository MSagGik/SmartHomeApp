package io.github.msaggik.sp.api.language

interface LanguageSharedPreferences {
    suspend fun getLanguageSharedPreferences() : String
    suspend fun setLanguage(languageAdd: String)
}