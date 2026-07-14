package com.gestorgastos.app.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda el JWT y su fecha de expiración en SharedPreferences.
 * Se usa tanto para inyectar el token en cada request (AuthInterceptor)
 * como para decidir si la sesión sigue vigente (Login/Dashboard).
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wallettrack_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String, userId: Int, name: String, email: String) {
        // El token JWT dura 60 minutos (ver .env del API: JWT_EXPIRES_IN=60m)
        val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserName(): String? = prefs.getString(KEY_NAME, null)

    fun isSessionValid(): Boolean {
        val token = getToken() ?: return false
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return token.isNotEmpty() && System.currentTimeMillis() < expiresAt
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_EXPIRES_AT = "jwt_expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
    }
}
