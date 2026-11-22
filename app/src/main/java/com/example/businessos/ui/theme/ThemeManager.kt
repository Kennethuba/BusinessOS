package com.example.businessos.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/** Accent colours the owner can pick, for example to match their logo. White text reads clearly on every one. */
enum class AccentTheme(val label: String, val swLabel: String, val main: Color, val light: Color, val dark: Color) {
    AMBER("Amber", "Chungwa", Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFF92400E)),
    TEAL("Teal", "Kijani-bluu", Color(0xFF0F766E), Color(0xFF14B8A6), Color(0xFF115E59)),
    BLUE("Blue", "Buluu", Color(0xFF1D4ED8), Color(0xFF60A5FA), Color(0xFF1E40AF)),
    GREEN("Green", "Kijani", Color(0xFF15803D), Color(0xFF4ADE80), Color(0xFF166534)),
    PURPLE("Purple", "Zambarau", Color(0xFF6D28D9), Color(0xFFA78BFA), Color(0xFF5B21B6)),
    RED("Red", "Nyekundu", Color(0xFFB91C1C), Color(0xFFF87171), Color(0xFF991B1B)),
    ROSE("Rose", "Waridi", Color(0xFFBE185D), Color(0xFFF472B6), Color(0xFF9D174D)),
    SLATE("Slate", "Kijivu", Color(0xFF334155), Color(0xFF94A3B8), Color(0xFF1E293B))
}

object ThemeManager {
    val accent = mutableStateOf(AccentTheme.AMBER)

    fun setAccent(context: Context, theme: AccentTheme) {
        accent.value = theme
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("accent_theme", theme.name).apply()
    }

    // Default to light theme (clean white background) as requested
    val isDarkMode = mutableStateOf(false)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        isDarkMode.value = prefs.getBoolean("is_dark_mode", false)
        accent.value = try { AccentTheme.valueOf(prefs.getString("accent_theme", "AMBER") ?: "AMBER") } catch (e: Exception) { AccentTheme.AMBER }
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
