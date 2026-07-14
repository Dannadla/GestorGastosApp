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
            repository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.LoginSuccess(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error de login") }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.register(name, email, password)
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
