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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    const val EDIT_EXPENSE = "edit_expense/{id}"
    fun editExpense(id: Int) = "edit_expense/$id"
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
                        tokenManager = tokenManager
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
    tokenManager: TokenManager
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
            // Se lee el nombre aquí (no al arrancar la app), así ya está guardado
            // tras el login y el saludo muestra el nombre real del usuario.
            DashboardScreen(
                expenseViewModel = expenseViewModel,
                userName = tokenManager.getUserName(),
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onEditExpense = { id -> navController.navigate(Routes.editExpense(id)) },
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

        composable(
            route = Routes.EDIT_EXPENSE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            // Se captura el movimiento UNA sola vez. Si observáramos la lista, al
            // borrar/editar el elemento desaparecería y se dispararía un segundo
            // popBackStack que vaciaba la navegación (pantalla en blanco).
            val expense = remember(id) {
                id?.let { targetId ->
                    expenseViewModel.uiState.value.expenses.find { it.id == targetId }
                }
            }

            if (expense != null) {
                AddExpenseScreen(
                    expenseViewModel = expenseViewModel,
                    editing = expense,
                    onSaved = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            } else {
                // Si no se encuentra el movimiento (p. ej. tras recargar), volvemos.
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
    }
}
