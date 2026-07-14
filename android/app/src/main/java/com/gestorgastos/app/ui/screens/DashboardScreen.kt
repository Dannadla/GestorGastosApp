package com.gestorgastos.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gestorgastos.app.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenseViewModel: ExpenseViewModel,
    userName: String?,
    onAddExpense: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by expenseViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { expenseViewModel.loadDashboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, ${userName ?: "usuario"}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Text("Resumen por categoría", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            val byCategory = uiState.summary?.byCategory.orEmpty()
            if (byCategory.isNotEmpty()) {
                BarChart(data = byCategory.associate { it.category to it.total })
            } else {
                Text("Aún no hay gastos registrados. Agita el celular o toca + para agregar uno.")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Movimientos recientes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.expenses) { expense ->
                    ListItem(
                        headlineContent = { Text("${expense.category} - $${expense.amount}") },
                        supportingContent = { Text("${expense.type} · ${expense.date}") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Gráfico de barras simple dibujado con Canvas de Compose, para no depender
 * de librerías externas de gráficos y mantener el proyecto liviano.
 */
@Composable
private fun BarChart(data: Map<String, Double>) {
    val maxValue = (data.values.maxOrNull() ?: 1.0).toFloat()
    val barColor = Color(0xFF4CAF50)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barWidth = size.width / (data.size * 2f)
            var x = barWidth / 2

            data.values.forEach { value ->
                val barHeight = (value.toFloat() / maxValue) * size.height
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
                x += barWidth * 2
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.keys.forEach { category ->
                Text(category, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
