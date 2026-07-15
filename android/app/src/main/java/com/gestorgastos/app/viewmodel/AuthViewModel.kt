package com.gestorgastos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestorgastos.app.data.model.LoginResponse
import com.gestorgastos.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class LoginSuccess(val data: LoginResponse) : AuthUiState()
    data class RegisterSuccess(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // trim() elimina espacios y saltos de línea al inicio/fin (p. ej. si el
            // teclado o un pegado dejó un Enter). La contraseña se envía tal cual.
            repository.login(email.trim(), password)
                .onSuccess { _uiState.value = AuthUiState.LoginSuccess(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error de login") }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // trim() evita guardar el salto de línea (Enter) o espacios sobrantes
            // en el nombre y el correo.
            repository.register(name.trim(), email.trim(), password)
                .onSuccess { _uiState.value = AuthUiState.RegisterSuccess(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error al registrar") }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
