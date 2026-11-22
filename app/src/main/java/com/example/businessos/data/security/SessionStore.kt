package com.example.businessos.data.security

import android.content.Context

/**
 * Remembers WHO was signed in (ids only, never a PIN) so the session survives the app
 * being killed. The app always starts locked and asks for the PIN again.
 */
interface SessionStore {
    fun save(userId: String, businessId: String?)
    fun load(): Pair<String, String?>?
    fun clear()

    object None : SessionStore {
        override fun save(userId: String, businessId: String?) = Unit
        override fun load(): Pair<String, String?>? = null
        override fun clear() = Unit
    }
}

class PrefsSessionStore(context: Context) : SessionStore {
    private val prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)

    override fun save(userId: String, businessId: String?) {
        prefs.edit().putString(KEY_USER, userId).putString(KEY_BUSINESS, businessId).apply()
    }

    override fun load(): Pair<String, String?>? {
        val user = prefs.getString(KEY_USER, null) ?: return null
        return user to prefs.getString(KEY_BUSINESS, null)
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER = "user_id"
        const val KEY_BUSINESS = "business_id"
    }
}
