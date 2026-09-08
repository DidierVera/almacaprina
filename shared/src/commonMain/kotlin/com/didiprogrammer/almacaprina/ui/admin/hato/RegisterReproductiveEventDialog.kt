package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_repro_event_add_kid_button
import almacaprina.shared.generated.resources.admin_repro_event_buck_label
import almacaprina.shared.generated.resources.admin_repro_event_dialog_title
import almacaprina.shared.generated.resources.admin_repro_event_kid_records_label
import almacaprina.shared.generated.resources.admin_repro_event_kid_sex_female_short
import almacaprina.shared.generated.resources.admin_repro_event_kid_sex_male_short
import almacaprina.shared.generated.resources.admin_repro_event_kid_tag_label
import almacaprina.shared.generated.resources.admin_repro_event_kids_alive_label
import almacaprina.shared.generated.resources.admin_repro_event_kids_born_label
import almacaprina.shared.generated.resources.admin_repro_event_no_change_option
import almacaprina.shared.generated.resources.admin_repro_event_remove_kid_content_description
import almacaprina.shared.generated.resources.admin_repro_event_result_label
import almacaprina.shared.generated.resources.admin_repro_event_resulting_status_label
import almacaprina.shared.generated.resources.admin_repro_event_type_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import almacaprina.shared.generated.resources.repro_event_result_failed
import almacaprina.shared.generated.resources.repro_event_result_pending
import almacaprina.shared.generated.resources.repro_event_result_successful
import almacaprina.shared.generated.resources.repro_event_type_abortion
import almacaprina.shared.generated.resources.repro_event_type_birth
import almacaprina.shared.generated.resources.repro_event_type_breeding
import almacaprina.shared.generated.resources.repro_event_type_heat_detected
import almacaprina.shared.generated.resources.repro_event_type_pregnancy_diagnosis
import almacaprina.shared.generated.resources.weighing_entry_date_label
import almacaprina.shared.generated.resources.weighing_entry_notes_label
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
import org.jetbrains.compose.resources.stringResource

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
        title = { Text(stringResource(Res.string.admin_repro_event_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateField(label = stringResource(Res.string.weighing_entry_date_label), date = date, onDateSelected = { date = it })

                Text(stringResource(Res.string.admin_repro_event_type_label))
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
                    Text(stringResource(Res.string.admin_repro_event_buck_label))
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

                Text(stringResource(Res.string.admin_repro_event_result_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ReproductiveEventResult.entries) { r ->
                        FilterChip(
                            selected = result == r,
                            onClick = { result = r },
                            label = { Text(r.spanishLabel()) }
                        )
                    }
                }

                Text(stringResource(Res.string.admin_repro_event_resulting_status_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = resultingDoeStatus == null,
                            onClick = { resultingDoeStatus = null },
                            label = { Text(stringResource(Res.string.admin_repro_event_no_change_option)) }
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
                        label = { Text(stringResource(Res.string.admin_repro_event_kids_born_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kidsAlive,
                        onValueChange = { kidsAlive = it },
                        label = { Text(stringResource(Res.string.admin_repro_event_kids_alive_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (result == ReproductiveEventResult.SUCCESSFUL) {
                        HorizontalDivider()
                        Text(stringResource(Res.string.admin_repro_event_kid_records_label))
                        newKidNames.indices.forEach { index ->
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = newKidNames[index],
                                    onValueChange = { newKidNames[index] = it },
                                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newKidTags[index],
                                    onValueChange = { newKidTags[index] = it },
                                    label = { Text(stringResource(Res.string.admin_repro_event_kid_tag_label)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                FilterChip(
                                    selected = newKidSexes[index] == GoatSex.FEMALE,
                                    onClick = {
                                        newKidSexes[index] = if (newKidSexes[index] == GoatSex.FEMALE) GoatSex.MALE else GoatSex.FEMALE
                                    },
                                    label = { Text(stringResource(if (newKidSexes[index] == GoatSex.FEMALE) Res.string.admin_repro_event_kid_sex_female_short else Res.string.admin_repro_event_kid_sex_male_short)) }
                                )
                                IconButton(onClick = {
                                    newKidNames.removeAt(index)
                                    newKidTags.removeAt(index)
                                    newKidSexes.removeAt(index)
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.admin_repro_event_remove_kid_content_description))
                                }
                            }
                        }
                        TextButton(onClick = {
                            newKidNames.add("")
                            newKidTags.add("")
                            newKidSexes.add(GoatSex.FEMALE)
                        }) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Text(" " + stringResource(Res.string.admin_repro_event_add_kid_button))
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(Res.string.weighing_entry_notes_label)) },
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
            }) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}

@Composable
private fun ReproductiveEventType.spanishLabel(): String = stringResource(
    when (this) {
        ReproductiveEventType.HEAT_DETECTED -> Res.string.repro_event_type_heat_detected
        ReproductiveEventType.BREEDING -> Res.string.repro_event_type_breeding
        ReproductiveEventType.PREGNANCY_DIAGNOSIS -> Res.string.repro_event_type_pregnancy_diagnosis
        ReproductiveEventType.BIRTH -> Res.string.repro_event_type_birth
        ReproductiveEventType.ABORTION -> Res.string.repro_event_type_abortion
    }
)

@Composable
private fun ReproductiveEventResult.spanishLabel(): String = stringResource(
    when (this) {
        ReproductiveEventResult.PENDING -> Res.string.repro_event_result_pending
        ReproductiveEventResult.SUCCESSFUL -> Res.string.repro_event_result_successful
        ReproductiveEventResult.FAILED -> Res.string.repro_event_result_failed
    }
)
