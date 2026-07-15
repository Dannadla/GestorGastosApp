package com.gestorgastos.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val DATE_FORMAT = "yyyy-MM-dd"

private fun utcFormatter(): SimpleDateFormat =
    SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

/** Convierte millis (UTC, como los entrega el DatePicker) a "yyyy-MM-dd". */
private fun millisToDate(millis: Long): String = utcFormatter().format(millis)

/** Convierte "yyyy-MM-dd" a millis UTC (o null si no parsea). */
private fun dateToMillis(date: String?): Long? {
    if (date.isNullOrBlank()) return null
    return try {
        utcFormatter().parse(date)?.time
    } catch (e: Exception) {
        null
    }
}

/** Millis UTC de medianoche de hoy: sirve de tope para no permitir fechas futuras. */
private fun todayUtcMillis(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** SelectableDates que solo admite hoy o fechas pasadas. */
@OptIn(ExperimentalMaterial3Api::class)
private object PastOrTodaySelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= todayUtcMillis()

    override fun isSelectableYear(year: Int): Boolean =
        year <= Calendar.getInstance().get(Calendar.YEAR)
}

/** Diálogo de calendario compartido; no permite fechas futuras. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovementDatePickerDialog(
    initial: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = dateToMillis(initial) ?: todayUtcMillis(),
        selectableDates = PastOrTodaySelectableDates
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(millisToDate(it)) }
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = state)
    }
}

/**
 * Campo de solo lectura que abre un DatePicker al tocarlo.
 * [value]/[onValueChange] usan formato "yyyy-MM-dd". No admite fechas futuras.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Elegir fecha") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.matchParentSize().clickable { showDialog = true })
    }

    if (showDialog) {
        MovementDatePickerDialog(
            initial = value,
            onDismiss = { showDialog = false },
            onConfirm = onValueChange
        )
    }
}

/**
 * Botón compacto de fecha (para filtros). Muestra la fecha elegida o el [label].
 * No admite fechas futuras.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDateButton(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.width(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            value ?: label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }

    if (showDialog) {
        MovementDatePickerDialog(
            initial = value,
            onDismiss = { showDialog = false },
            onConfirm = onValueChange
        )
    }
}
