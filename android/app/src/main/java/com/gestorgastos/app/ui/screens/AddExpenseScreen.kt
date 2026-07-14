package com.gestorgastos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gestorgastos.app.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    onSaved: () -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("gasto") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Agregar movimiento", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        SingleChoiceSegment(
            options = listOf("gasto", "ingreso"),
            selected = type,
            onSelected = { type = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría (comida, transporte, etc.)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull() ?: 0.0
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                expenseViewModel.addExpense(category, amountValue, description, type, today)
                onSaved()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = category.isNotBlank() && amount.toDoubleOrNull() != null
        ) {
            Text("Guardar")
        }
    }
}

@Composable
private fun SingleChoiceSegment(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(option.replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
