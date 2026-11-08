package com.example.businessos.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf

enum class AppLanguage {
    ENGLISH,
    SWAHILI
}

object AppLanguageManager {
    val currentLanguage = mutableStateOf(AppLanguage.ENGLISH)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val saved = prefs.getString("selected_language", AppLanguage.ENGLISH.name)
        currentLanguage.value = try {
            AppLanguage.valueOf(saved ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        currentLanguage.value = language
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", language.name).apply()
    }

    fun toggleLanguage(context: Context) {
        val next = if (currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.SWAHILI else AppLanguage.ENGLISH
        setLanguage(context, next)
    }

    fun isEnglish(): Boolean = currentLanguage.value == AppLanguage.ENGLISH

    /**
     * Helper function to return English by default or Swahili if selected.
     * Custom user-created content (e.g. stock item names, custom ledger account names)
     * is preserved exactly as created by the user.
     */
    fun tr(en: String, sw: String): String {
        return if (isEnglish()) en else sw
    }
}
