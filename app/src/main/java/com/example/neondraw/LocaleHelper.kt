package com.example.neondraw

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "neon_draw_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_EN = "en"
    const val LANG_VI = "vi"

    private var currentLang: String = LANG_EN

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default language is English ("en") as requested
        currentLang = prefs.getString(KEY_LANGUAGE, LANG_EN) ?: LANG_EN
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANGUAGE, LANG_EN) ?: LANG_EN
        currentLang = lang
        return lang
    }

    fun isVietnamese(): Boolean {
        return currentLang == LANG_VI
    }

    fun setLanguage(context: Context, lang: String): Context {
        currentLang = lang
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        return applyLocale(context, lang)
    }

    fun applyLocale(context: Context, lang: String = currentLang): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

