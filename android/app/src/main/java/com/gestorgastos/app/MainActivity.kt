package com.gestorgastos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gestorgastos.app.data.network.RetrofitClient
import com.gestorgastos.app.data.network.TokenManager
import com.gestorgastos.app.data.repository.AuthRepository
import com.gestorgastos.app.data.repository.ExpenseRepository
import com.gestorgastos.app.sensor.ShakeDetector
import com.gestorgastos.app.ui.screens.AddExpenseScreen
import com.gestorgastos.app.ui.screens.DashboardScreen
import com.gestorgastos.app.ui.screens.LoginScreen
import com.gestorgastos.app.ui.screens.RegisterScreen
import com.gestorgastos.app.viewmodel.AuthViewModel
import com.gestorgastos.app.viewmodel.ExpenseViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense"
}

class MainActivity : ComponentActivity() {

    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        val apiService = RetrofitClient.create(tokenManager)
        val authRepository = AuthRepository(apiService, tokenManager)
        val expenseRepository = ExpenseRepository(apiService)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier) {
                    val navController = rememberNavController()
                    val authViewModel = viewModel { AuthViewModel(authRepository) }
                    val expenseViewModel = viewModel { ExpenseViewModel(expenseRepository) }

                    // Acelerómetro: agitar el celular en el Dashboard abre "Agregar gasto"
                    DisposableEffect(Unit) {
                        shakeDetector = ShakeDetector(applicationContext) {
                            if (navController.currentDestination?.route == Routes.DASHBOARD) {
                                navController.navigate(Routes.ADD_EXPENSE)
                            }
                        }
                        shakeDetector.start()
                        onDispose { shakeDetector.stop() }
                    }

                    val startDestination =
                        if (authRepository.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        authViewModel = authViewModel,
                        expenseViewModel = expenseViewModel,
                        userName = tokenManager.getUserName()
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel,
    expenseViewModel: ExpenseViewModel,
    userName: String?
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                expenseViewModel = expenseViewModel,
                userName = userName,
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_EXPENSE) {
            AddExpenseScreen(
                expenseViewModel = expenseViewModel,
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
