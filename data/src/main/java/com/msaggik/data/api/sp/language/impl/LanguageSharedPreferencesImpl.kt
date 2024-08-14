package com.msaggik.data.api.sp.language.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.msaggik.common_ui.R
import com.msaggik.data.api.sp.language.LanguageSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LANGUAGE_APP_KEY = "language_app_key"
class LanguageSharedPreferencesImpl(
    private val context: Context,
    private val sp: SharedPreferences
) : LanguageSharedPreferences {

    override suspend fun getLanguageSharedPreferences(): String {
        return sp.getString(LANGUAGE_APP_KEY, context.getString(R.string.default_)).toString()
    }

    override suspend fun setLanguage(languageAdd: String) {
        sp.edit().putString(LANGUAGE_APP_KEY, languageAdd).apply()
        setLanguageApplication(languageAdd)
    }

    override suspend fun setLanguageApplication(language: String) {
        withContext(Dispatchers.Main) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}