package com.msaggik.data.api.sp.language

interface LanguageSharedPreferences {
    suspend fun getLanguageSharedPreferences() : String
    suspend fun setLanguage(languageAdd: String)
    suspend fun setLanguageApplication(language: String)
}