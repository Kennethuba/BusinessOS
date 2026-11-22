package com.example.businessos.data.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Decides when the app asks for the PIN. The app starts locked, and locks again after
 * being in the background longer than the chosen timeout.
 */
class AppLock(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("app_lock", Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _timeoutMillis = MutableStateFlow(prefs.getLong(KEY_TIMEOUT, DEFAULT_TIMEOUT))
    val timeoutMillis: StateFlow<Long> = _timeoutMillis.asStateFlow()

    private var backgroundedAt: Long = 0L

    fun lock() { _isLocked.value = true }

    fun unlock() { _isLocked.value = false }

    fun setTimeout(millis: Long) {
        _timeoutMillis.value = millis
        prefs.edit().putLong(KEY_TIMEOUT, millis).apply()
    }

    fun onBackgrounded(now: Long = System.currentTimeMillis()) {
        backgroundedAt = now
    }

    fun onForegrounded(now: Long = System.currentTimeMillis()) {
        if (backgroundedAt == 0L) return
        if (now - backgroundedAt >= _timeoutMillis.value) lock()
        backgroundedAt = 0L
    }

    companion object {
        const val IMMEDIATELY = 0L
        const val ONE_MINUTE = 60_000L
        const val FIVE_MINUTES = 5 * 60_000L
        const val FIFTEEN_MINUTES = 15 * 60_000L
        const val DEFAULT_TIMEOUT = FIVE_MINUTES
        private const val KEY_TIMEOUT = "timeout_millis"
    }
}
