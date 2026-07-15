package com.gestorgastos.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gestorgastos.app.ui.components.CompactDateButton
import com.gestorgastos.app.viewmodel.ExpenseViewModel
import java.util.Locale

private val INGRESO_COLOR = Color(0xFF2E7D32)
private val GASTO_COLOR = Color(0xFFC62828)
private val DISPONIBLE_COLOR = Color(0xFFBDBDBD)

// Paleta para diferenciar categorías en el gráfico de dona.
private val CATEGORY_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFF9C27B0),
    Color(0xFFF44336), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFF795548),
    Color(0xFF607D8B), Color(0xFFE91E63)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenseViewModel: ExpenseViewModel,
    userName: String?,
    onAddExpense: () -> Unit,
    onEditExpense: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by expenseViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { expenseViewModel.loadDashboard() }

    var fromDate by remember { mutableStateOf<String?>(null) }
    var toDate by remember { mutableStateOf<String?>(null) }

    val filtered = uiState.expenses.filter { e ->
        (fromDate == null || e.date >= fromDate!!) && (toDate == null || e.date <= toDate!!)
    }

    val ingresos = filtered.filter { it.type == "ingreso" }.sumOf { it.amount }
    val gastos = filtered.filter { it.type == "gasto" }.sumOf { it.amount }
    val byCategory = filtered
        .filter { it.type == "gasto" }
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

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
                Icon(Icons.Default.Add, contentDescription = "Agregar movimiento")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {

            item {
                SummaryCards(ingresos = ingresos, gastos = gastos)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DateFilter(
                    fromDate = fromDate,
                    toDate = toDate,
                    onFromChange = { fromDate = it },
                    onToChange = { toDate = it },
                    onClear = { fromDate = null; toDate = null }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Gasto vs. ingresos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (byCategory.isNotEmpty() || ingresos > 0) {
                    DonutChart(ingresos = ingresos, categories = byCategory)
                } else {
                    Text("Aún no hay datos en este rango. Toca + para agregar un movimiento.")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text("Movimientos recientes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.isLoading) CircularProgressIndicator()
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (!uiState.isLoading && filtered.isEmpty()) {
                    Text("No hay movimientos para mostrar.")
                }
            }

            items(filtered) { expense ->
                val isIngreso = expense.type == "ingreso"
                val name = expense.description?.takeIf { it.isNotBlank() } ?: expense.category
                ListItem(
                    modifier = Modifier.clickable { onEditExpense(expense.id) },
                    headlineContent = { Text(name) },
                    supportingContent = { Text("${expense.category} · ${expense.date}") },
                    trailingContent = {
                        Text(
                            (if (isIngreso) "+" else "-") + formatMoney(expense.amount),
                            fontWeight = FontWeight.Bold,
                            color = if (isIngreso) INGRESO_COLOR else GASTO_COLOR
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

/** Fila con dos tarjetas: total de ingresos y total de gastos. */
@Composable
private fun SummaryCards(ingresos: Double, gastos: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Ingresos",
            amount = ingresos,
            icon = Icons.Default.KeyboardArrowUp,
            accent = INGRESO_COLOR
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Gastos",
            amount = gastos,
            icon = Icons.Default.KeyboardArrowDown,
            accent = GASTO_COLOR
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    accent: Color
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                formatMoney(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

/** Filtro compacto por rango de fechas (no admite fechas futuras). */
@Composable
private fun DateFilter(
    fromDate: String?,
    toDate: String?,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column {
        Text("Filtrar por fecha", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactDateButton(
                label = "Desde",
                value = fromDate,
                onValueChange = onFromChange,
                modifier = Modifier.weight(1f)
            )
            CompactDateButton(
                label = "Hasta",
                value = toDate,
                onValueChange = onToChange,
                modifier = Modifier.weight(1f)
            )
            if (fromDate != null || toDate != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro")
                }
            }
        }
    }
}

/**
 * Dona que compara el gasto consumido frente al total de ingresos.
 * El anillo completo representa los ingresos; cada categoría de gasto es un
 * sector de color y el resto ("Disponible") queda en gris. En el centro se
 * muestra el porcentaje del ingreso ya consumido.
 */
@Composable
private fun DonutChart(ingresos: Double, categories: List<Pair<String, Double>>) {
    val gastos = categories.sumOf { it.second }
    val base = maxOf(ingresos, gastos)
    if (base <= 0.0) {
        Text("Sin datos para graficar.")
        return
    }
    val disponible = (ingresos - gastos).coerceAtLeast(0.0)
    val consumidoPct = if (ingresos > 0) (gastos / ingresos * 100).toInt() else 100

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val stroke = size.minDimension * 0.20f
                val diameter = size.minDimension - stroke
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                var startAngle = -90f
                categories.forEachIndexed { index, (_, value) ->
                    val sweep = (value / base * 360.0).toFloat()
                    drawArc(
                        color = CATEGORY_COLORS[index % CATEGORY_COLORS.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    startAngle += sweep
                }
                if (disponible > 0.0) {
                    drawArc(
                        color = DISPONIBLE_COLOR,
                        startAngle = startAngle,
                        sweepAngle = (disponible / base * 360.0).toFloat(),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$consumidoPct%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("consumido", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            categories.forEachIndexed { index, (category, value) ->
                LegendRow(
                    color = CATEGORY_COLORS[index % CATEGORY_COLORS.size],
                    label = category,
                    amount = value
                )
            }
            if (disponible > 0.0) {
                LegendRow(color = DISPONIBLE_COLOR, label = "Disponible", amount = disponible)
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, amount: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(formatMoney(amount), style = MaterialTheme.typography.labelMedium)
    }
}

/** Formatea un monto como moneda: 1234.5 -> "$1,234.50". */
private fun formatMoney(value: Double): String =
    String.format(Locale.getDefault(), "$%,.2f", value)
