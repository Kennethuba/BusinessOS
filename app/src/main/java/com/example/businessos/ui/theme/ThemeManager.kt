package com.example.businessos.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    // Default to light theme (clean white background) as requested
    val isDarkMode = mutableStateOf(false)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        isDarkMode.value = prefs.getBoolean("is_dark_mode", false)
    }

    fun setDarkMode(context: Context, dark: Boolean) {
        isDarkMode.value = dark
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_mode", dark).apply()
    }

    fun toggleTheme(context: Context) {
        setDarkMode(context, !isDarkMode.value)
    }
}
