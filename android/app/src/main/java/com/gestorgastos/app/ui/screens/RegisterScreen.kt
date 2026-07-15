package com.gestorgastos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gestorgastos.app.viewmodel.AuthUiState
import com.gestorgastos.app.viewmodel.AuthViewModel

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validaciones de campos requeridos.
    val nombreValido = name.isNotBlank()
    val emailValido = EMAIL_REGEX.matches(email)
    val passwordValido = password.length >= 6
    val formularioValido = nombreValido && emailValido && passwordValido

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.RegisterSuccess) {
            onRegisterSuccess()
            authViewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            // Se descartan saltos de línea por si el teclado llega a insertar alguno.
            onValueChange = { name = it.replace("\n", "") },
            label = { Text("Nombre completo *") },
            singleLine = true,
            isError = name.isNotEmpty() && !nombreValido,
            supportingText = {
                if (name.isNotEmpty() && !nombreValido) Text("El nombre es requerido")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.replace("\n", "") },
            label = { Text("Correo electrónico *") },
            singleLine = true,
            isError = email.isNotEmpty() && !emailValido,
            supportingText = {
                if (email.isNotEmpty() && !emailValido) Text("Ingresa un correo válido")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.replace("\n", "") },
            label = { Text("Contraseña *") },
            singleLine = true,
            isError = password.isNotEmpty() && !passwordValido,
            supportingText = {
                if (password.isNotEmpty() && !passwordValido) Text("Mínimo 6 caracteres")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { authViewModel.register(name, email, password) },
                enabled = formularioValido,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onGoToLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
