package com.gestorgastos.app.data.repository

import com.gestorgastos.app.data.model.LoginRequest
import com.gestorgastos.app.data.model.LoginResponse
import com.gestorgastos.app.data.model.RegisterRequest
import com.gestorgastos.app.data.network.ApiService
import com.gestorgastos.app.data.network.TokenManager

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val response = api.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Registro exitoso")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al registrar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(body.token, body.userId, body.name, body.email)
                Result.success(body)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.logout()
    }

    fun isLoggedIn(): Boolean = tokenManager.isSessionValid()
}
