package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.ui.components.DateField
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Formulario rápido para registrar una pesada, invocado desde la pestaña "Peso" de la ficha técnica. */
@Composable
fun RegisterWeightDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, weightKg: Double, bodyConditionScore: Int?, notes: String?) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var weightText by remember { mutableStateOf("") }
    var bcs by remember { mutableStateOf<Int?>(null) }
    var notes by remember { mutableStateOf("") }
    val weight = weightText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar pesada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateField(label = "Fecha", date = date, onDateSelected = { date = it })
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Body Condition Score (1-5, opcional)")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { score ->
                        FilterChip(
                            selected = bcs == score,
                            onClick = { bcs = if (bcs == score) null else score },
                            label = { Text(score.toString()) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = weight != null && weight > 0,
                onClick = { weight?.let { onSave(date, it, bcs, notes.ifBlank { null }) } }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
