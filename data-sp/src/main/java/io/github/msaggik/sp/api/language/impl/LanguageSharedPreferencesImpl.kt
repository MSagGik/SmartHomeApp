package io.github.msaggik.sp.api.language.impl

import android.content.Context
import android.content.SharedPreferences
import io.github.msaggik.sp.api.language.LanguageSharedPreferences

private const val LANGUAGE_APP_KEY = "language_app_key"
class LanguageSharedPreferencesImpl(
    private val context: Context,
    private val sp: SharedPreferences
) : LanguageSharedPreferences {

    override suspend fun getLanguageSharedPreferences(): String {
        return sp.getString(LANGUAGE_APP_KEY, context.getString(io.github.msaggik.ui.R.string.default_)).toString()
    }

    override suspend fun setLanguage(languageAdd: String) {
        sp.edit().putString(LANGUAGE_APP_KEY, languageAdd).apply()
    }
}