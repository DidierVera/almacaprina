package com.didiprogrammer.almacaprina.ui.admin.hato

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.label
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Ficha mínima de un cabrito a crear junto con el evento de parto. */
data class NewKidEntry(val name: String, val tagNumber: String, val sex: GoatSex)

data class ReproductiveEventFormResult(
    val date: LocalDate,
    val eventType: ReproductiveEventType,
    val buckId: String?,
    val result: ReproductiveEventResult,
    val kidsBornCount: Int?,
    val kidsAliveCount: Int?,
    val notes: String?,
    val newKids: List<NewKidEntry> = emptyList(),
    val resultingDoeStatus: GoatStatus? = null
)

/**
 * Formulario rápido para registrar un evento reproductivo, invocado desde la pestaña
 * "Reproducción" de la ficha técnica. Si el evento es un parto, permite crear de una vez
 * la ficha técnica de cada cabrito nacido — el evento queda vinculado a esas fichas nuevas
 * (kid_ids), tal como pide docs/data_model.md.
 *
 * El estado resultante de la cabra es SIEMPRE de definición manual (decisión explícita del
 * dueño): un aborto, por ejemplo, puede dejarla seca o en producción según en qué etapa de
 * la gestación ocurrió, así que no se puede inferir automáticamente del tipo de evento.
 */
@Composable
fun RegisterReproductiveEventDialog(
    bucks: List<Goat>,
    onDismiss: () -> Unit,
    onSave: (ReproductiveEventFormResult) -> Unit
) {
    var date by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var eventType by remember { mutableStateOf(ReproductiveEventType.BREEDING) }
    var buckId by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf(ReproductiveEventResult.PENDING) }
    var kidsBorn by remember { mutableStateOf("") }
    var kidsAlive by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var resultingDoeStatus by remember { mutableStateOf<GoatStatus?>(null) }
    val newKidNames = remember { mutableStateListOf<String>() }
    val newKidTags = remember { mutableStateListOf<String>() }
    val newKidSexes = remember { mutableStateListOf<GoatSex>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar evento reproductivo") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateField(label = "Fecha", date = date, onDateSelected = { date = it })

                Text("Tipo de evento")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ReproductiveEventType.entries) { type ->
                        FilterChip(
                            selected = eventType == type,
                            onClick = { eventType = type },
                            label = { Text(type.spanishLabel()) }
                        )
                    }
                }

                if (eventType == ReproductiveEventType.BREEDING) {
                    Text("Semental (opcional)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(bucks) { buck ->
                            FilterChip(
                                selected = buckId == buck.id,
                                onClick = { buckId = if (buckId == buck.id) null else buck.id },
                                label = { Text(buck.name) }
                            )
                        }
                    }
                }

                Text("Resultado")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ReproductiveEventResult.entries) { r ->
                        FilterChip(
                            selected = result == r,
                            onClick = { result = r },
                            label = { Text(r.spanishLabel()) }
                        )
                    }
                }

                Text("Estado resultante de la cabra (opcional, tú decides)")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = resultingDoeStatus == null,
                            onClick = { resultingDoeStatus = null },
                            label = { Text("Sin cambio") }
                        )
                    }
                    items(
                        listOf(
                            GoatStatus.IN_PRODUCTION,
                            GoatStatus.PREGNANT,
                            GoatStatus.DRY,
                            GoatStatus.RETIRED,
                            GoatStatus.DECEASED
                        )
                    ) { status ->
                        FilterChip(
                            selected = resultingDoeStatus == status,
                            onClick = { resultingDoeStatus = status },
                            label = { Text(status.label()) }
                        )
                    }
                }

                if (eventType == ReproductiveEventType.BIRTH) {
                    OutlinedTextField(
                        value = kidsBorn,
                        onValueChange = { kidsBorn = it },
                        label = { Text("Cabritos nacidos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kidsAlive,
                        onValueChange = { kidsAlive = it },
                        label = { Text("Cabritos vivos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (result == ReproductiveEventResult.SUCCESSFUL) {
                        HorizontalDivider()
                        Text("Fichas de los cabritos (opcional, se crean junto con este evento)")
                        newKidNames.indices.forEach { index ->
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = newKidNames[index],
                                    onValueChange = { newKidNames[index] = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newKidTags[index],
                                    onValueChange = { newKidTags[index] = it },
                                    label = { Text("Arete") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                FilterChip(
                                    selected = newKidSexes[index] == GoatSex.FEMALE,
                                    onClick = {
                                        newKidSexes[index] = if (newKidSexes[index] == GoatSex.FEMALE) GoatSex.MALE else GoatSex.FEMALE
                                    },
                                    label = { Text(if (newKidSexes[index] == GoatSex.FEMALE) "H" else "M") }
                                )
                                IconButton(onClick = {
                                    newKidNames.removeAt(index)
                                    newKidTags.removeAt(index)
                                    newKidSexes.removeAt(index)
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Quitar")
                                }
                            }
                        }
                        TextButton(onClick = {
                            newKidNames.add("")
                            newKidTags.add("")
                            newKidSexes.add(GoatSex.FEMALE)
                        }) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Text(" Agregar ficha de cabrito")
                        }
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
            TextButton(onClick = {
                val newKids = newKidNames.indices
                    .map { i -> NewKidEntry(newKidNames[i].trim(), newKidTags[i].trim(), newKidSexes[i]) }
                    .filter { it.name.isNotBlank() && it.tagNumber.isNotBlank() }
                onSave(
                    ReproductiveEventFormResult(
                        date = date,
                        eventType = eventType,
                        buckId = buckId,
                        result = result,
                        kidsBornCount = kidsBorn.toIntOrNull(),
                        kidsAliveCount = kidsAlive.toIntOrNull(),
                        notes = notes.ifBlank { null },
                        newKids = newKids,
                        resultingDoeStatus = resultingDoeStatus
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun ReproductiveEventType.spanishLabel(): String = when (this) {
    ReproductiveEventType.HEAT_DETECTED -> "Celo detectado"
    ReproductiveEventType.BREEDING -> "Monta"
    ReproductiveEventType.PREGNANCY_DIAGNOSIS -> "Diagnóstico de preñez"
    ReproductiveEventType.BIRTH -> "Parto"
    ReproductiveEventType.ABORTION -> "Aborto"
}

private fun ReproductiveEventResult.spanishLabel(): String = when (this) {
    ReproductiveEventResult.PENDING -> "Pendiente"
    ReproductiveEventResult.SUCCESSFUL -> "Exitoso"
    ReproductiveEventResult.FAILED -> "Fallido"
}
