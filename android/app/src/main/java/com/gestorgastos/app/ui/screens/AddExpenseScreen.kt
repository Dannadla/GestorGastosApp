package com.gestorgastos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gestorgastos.app.data.model.Expense
import com.gestorgastos.app.ui.components.DatePickerField
import com.gestorgastos.app.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

// Categorías controladas según el tipo de movimiento.
private val CATEGORIAS_GASTO = listOf(
    "Comida", "Transporte", "Servicios", "Entretenimiento",
    "Salud", "Educación", "Hogar", "Compras", "Otros"
)
private val CATEGORIAS_INGRESO = listOf(
    "Salario", "Ventas", "Regalos", "Inversiones", "Otros"
)

// Monto monetario: dígitos y como máximo un punto decimal con hasta 2 decimales.
private val MONTO_REGEX = Regex("^\\d{0,9}(\\.\\d{0,2})?$")

private fun montoInicial(expense: Expense?): String {
    val amount = expense?.amount ?: return ""
    return if (amount % 1.0 == 0.0) amount.toLong().toString()
    else amount.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    editing: Expense? = null,
    onSaved: () -> Unit,
    onDeleted: () -> Unit = onSaved
) {
    val hoy = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var type by remember { mutableStateOf(editing?.type ?: "gasto") }
    var category by remember { mutableStateOf(editing?.category ?: CATEGORIAS_GASTO.first()) }
    var name by remember { mutableStateOf(editing?.description ?: "") }
    var amount by remember { mutableStateOf(montoInicial(editing)) }
    var date by remember { mutableStateOf(editing?.date ?: hoy) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val categorias = if (type == "ingreso") CATEGORIAS_INGRESO else CATEGORIAS_GASTO

    // Validaciones de campos requeridos.
    val montoValido = amount.toDoubleOrNull()?.let { it > 0.0 } == true
    val nombreValido = name.isNotBlank()
    val formularioValido = nombreValido && montoValido && category.isNotBlank()

    val esEdicion = editing != null

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            if (esEdicion) "Editar movimiento" else "Agregar movimiento",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        SingleChoiceSegment(
            options = listOf("gasto", "ingreso"),
            selected = type,
            onSelected = { nuevoTipo ->
                type = nuevoTipo
                val nuevas = if (nuevoTipo == "ingreso") CATEGORIAS_INGRESO else CATEGORIAS_GASTO
                if (category !in nuevas) category = nuevas.first()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Categoría: dropdown controlado.
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categorias.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            category = opcion
                            categoryExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.replace("\n", "") },
            label = { Text("Nombre del movimiento *") },
            singleLine = true,
            isError = name.isNotEmpty() && !nombreValido,
            supportingText = {
                if (name.isNotEmpty() && !nombreValido) Text("Ingresa un nombre")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { nuevo -> if (MONTO_REGEX.matches(nuevo)) amount = nuevo },
            label = { Text("Monto *") },
            singleLine = true,
            isError = amount.isNotEmpty() && !montoValido,
            supportingText = {
                if (amount.isNotEmpty() && !montoValido) Text("Ingresa un monto válido mayor a 0")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        DatePickerField(
            label = "Fecha",
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull() ?: 0.0
                if (esEdicion) {
                    expenseViewModel.updateExpense(
                        editing!!.id, category, amountValue, name.trim(), type, date
                    )
                } else {
                    expenseViewModel.addExpense(category, amountValue, name.trim(), type, date)
                }
                onSaved()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = formularioValido
        ) {
            Text(if (esEdicion) "Guardar cambios" else "Guardar")
        }

        if (esEdicion) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar movimiento")
            }
        }
    }

    if (showDeleteDialog && editing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Seguro que deseas eliminar este movimiento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    expenseViewModel.deleteExpense(editing.id)
                    onDeleted()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
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
