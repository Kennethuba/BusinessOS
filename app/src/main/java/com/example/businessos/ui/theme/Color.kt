package com.example.businessos.ui.theme

import androidx.compose.ui.graphics.Color

// Adaptive theme backgrounds (Default: Crisp White)
val DarkBackground: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF0F172A) else Color(0xFFF8FAFC)

val DarkSurface: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val DarkSurfaceElevated: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF334155) else Color(0xFFF1F5F9)

val DarkBorder: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF334155) else Color(0xFFE2E8F0)

// Crisp high-contrast typography
val TextPrimary: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val TextSecondary: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF94A3B8) else Color(0xFF475569)

val TextMuted: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF64748B) else Color(0xFF64748B)

val TextOnAccent: Color = Color.White

// Structured cards & tags (No glowing neon gradients)
val CardBackground: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val CardBorder: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF334155) else Color(0xFFE2E8F0)

val TagBackground: Color
    get() = if (ThemeManager.isDarkMode.value) Color(0xFF334155) else Color(0xFFF1F5F9)

// Professional Business Accents
// The accent follows the colour the owner picked in More > Colour (default amber).
val AmberAccent: Color get() = ThemeManager.accent.value.main
val AmberAccentLight: Color get() = ThemeManager.accent.value.light
val AmberAccentDark: Color get() = ThemeManager.accent.value.dark

val TealAccent = Color(0xFF0F766E)
val TealAccentLight = Color(0xFF14B8A6)

// Authoritative Accounting Status Colors
val StatusGreen = Color(0xFF15803D)
val StatusAmber = Color(0xFFD97706)
val StatusBlue = Color(0xFF1D4ED8)
val StatusPurple = Color(0xFF4338CA)
val StatusRed = Color(0xFFDC2626)
