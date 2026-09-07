package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.ui.components.DateField
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class HealthEventFormResult(
    val date: LocalDate,
    val type: HealthRecordType,
    val description: String?,
    val insumoId: String?,
    val dosage: String?,
    val quantityUsed: Double?,
    val milkWithdrawalDays: Int?,
    val cost: Double?,
    val veterinarian: String?,
    val nextSuggestedDate: LocalDate?
)

/** Formulario rápido para registrar un evento de salud, invocado desde la pestaña "Salud". */
@Composable
fun RegisterHealthEventDialog(
    veterinaryInsumos: List<Insumo>,
    onDismiss: () -> Unit,
    onSave: (HealthEventFormResult) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var type by remember { mutableStateOf(HealthRecordType.VACCINE) }
    var description by remember { mutableStateOf("") }
    var insumoId by remember { mutableStateOf<String?>(null) }
    var dosage by remember { mutableStateOf("") }
    var quantityUsed by remember { mutableStateOf("") }
    var milkWithdrawalDays by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var veterinarian by remember { mutableStateOf("") }
    var nextSuggestedDate by remember { mutableStateOf<LocalDate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar evento de salud") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateField(label = "Fecha", date = date, onDateSelected = { date = it })

                Text("Tipo")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HealthRecordType.entries) { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.spanishLabel()) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (veterinaryInsumos.isNotEmpty()) {
                    Text("Insumo aplicado (opcional)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(veterinaryInsumos) { insumo ->
                            FilterChip(
                                selected = insumoId == insumo.id,
                                onClick = { insumoId = if (insumoId == insumo.id) null else insumo.id },
                                label = { Text(insumo.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosis (ej. \"5 ml\")") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantityUsed,
                    onValueChange = { quantityUsed = it },
                    label = { Text("Cantidad consumida (unidad del insumo)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = milkWithdrawalDays,
                    onValueChange = { milkWithdrawalDays = it },
                    label = { Text("Días de retiro de leche (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Costo (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = veterinarian,
                    onValueChange = { veterinarian = it },
                    label = { Text("Veterinario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                DateField(
                    label = "Próxima fecha sugerida (opcional)",
                    date = nextSuggestedDate,
                    onDateSelected = { nextSuggestedDate = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    HealthEventFormResult(
                        date = date,
                        type = type,
                        description = description.ifBlank { null },
                        insumoId = insumoId,
                        dosage = dosage.ifBlank { null },
                        quantityUsed = quantityUsed.toDoubleOrNull(),
                        milkWithdrawalDays = milkWithdrawalDays.toIntOrNull(),
                        cost = cost.toDoubleOrNull(),
                        veterinarian = veterinarian.ifBlank { null },
                        nextSuggestedDate = nextSuggestedDate
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun HealthRecordType.spanishLabel(): String = when (this) {
    HealthRecordType.VACCINE -> "Vacuna"
    HealthRecordType.DEWORMING -> "Desparasitación"
    HealthRecordType.TREATMENT -> "Tratamiento"
    HealthRecordType.ROUTINE_CHECKUP -> "Revisión de rutina"
    HealthRecordType.DIAGNOSIS -> "Diagnóstico"
}
