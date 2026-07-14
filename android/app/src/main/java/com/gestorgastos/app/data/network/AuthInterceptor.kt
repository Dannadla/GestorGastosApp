package com.gestorgastos.app.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega el header "Authorization: Bearer <token>" a cada request saliente,
 * usando el JWT guardado en TokenManager. Así el API puede validar la sesión
 * en cada llamada a las rutas protegidas de expenses.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.getToken()

        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
