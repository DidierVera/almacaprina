package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_goat_form_external_father_breed_label
import almacaprina.shared.generated.resources.admin_goat_form_father_external_label
import almacaprina.shared.generated.resources.admin_goat_form_father_internal_label
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_repro_event_add_kid_button
import almacaprina.shared.generated.resources.admin_repro_event_buck_from_breeding_label
import almacaprina.shared.generated.resources.admin_repro_event_buck_label
import almacaprina.shared.generated.resources.admin_repro_event_dialog_title
import almacaprina.shared.generated.resources.admin_repro_event_external_buck_name_label
import almacaprina.shared.generated.resources.admin_repro_event_edit_dialog_title
import almacaprina.shared.generated.resources.admin_repro_event_kid_number_label
import almacaprina.shared.generated.resources.admin_repro_event_kid_records_label
import almacaprina.shared.generated.resources.admin_repro_event_kid_sex_female_short
import almacaprina.shared.generated.resources.admin_repro_event_kid_sex_male_short
import almacaprina.shared.generated.resources.admin_repro_event_kid_tag_label
import almacaprina.shared.generated.resources.admin_repro_event_kids_alive_label
import almacaprina.shared.generated.resources.admin_repro_event_kids_already_registered_note
import almacaprina.shared.generated.resources.admin_repro_event_kids_born_label
import almacaprina.shared.generated.resources.admin_repro_event_no_change_option
import almacaprina.shared.generated.resources.admin_repro_event_remove_kid_content_description
import almacaprina.shared.generated.resources.admin_repro_event_result_label
import almacaprina.shared.generated.resources.admin_repro_event_resulting_status_label
import almacaprina.shared.generated.resources.admin_repro_event_type_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import almacaprina.shared.generated.resources.weighing_entry_date_label
import almacaprina.shared.generated.resources.weighing_entry_notes_label
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.model.BreedPercentage
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatSex
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
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
    val externalBuckName: String?,
    val externalBuckBreedComposition: List<BreedPercentage>?,
    val result: ReproductiveEventResult,
    val kidsBornCount: Int?,
    val kidsAliveCount: Int?,
    val notes: String?,
    val newKids: List<NewKidEntry> = emptyList(),
    val resultingDoeStatus: GoatStatus? = null
)

/** Resumen legible del semental (interno o externo) de la monta vinculada — solo informativo. */
private fun linkedBuckSummary(event: ReproductiveEvent, bucks: List<Goat>): String {
    val name = event.buckId?.let { id -> bucks.firstOrNull { it.id == id }?.name } ?: event.externalBuckName ?: ""
    val composition = event.buckId?.let { id -> bucks.firstOrNull { it.id == id }?.breedComposition } ?: event.externalBuckBreedComposition
    val compositionLabel = composition
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(" · ") { "${formatQuantity(it.percentage)}% ${it.breedName}" }
    return if (compositionLabel != null) "$name ($compositionLabel)" else name
}

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
    breeds: List<Breed>,
    /** Monta más reciente de esta cabra — si se abre el diálogo para un parto nuevo y ya trae
     * semental cargado, se reutiliza tal cual (ver CLAUDE.md); si no, se puede cargar aquí mismo. */
    linkedBreedingEvent: ReproductiveEvent? = null,
    existingEvent: ReproductiveEvent? = null,
    onDismiss: () -> Unit,
    onSave: (ReproductiveEventFormResult) -> Unit
) {
    var date by remember { mutableStateOf(existingEvent?.date ?: Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    var eventType by remember { mutableStateOf(existingEvent?.eventType ?: ReproductiveEventType.BREEDING) }
    var buckId by remember { mutableStateOf(existingEvent?.buckId) }
    var buckIsExternal by remember {
        mutableStateOf(existingEvent != null && existingEvent.buckId == null && !existingEvent.externalBuckName.isNullOrBlank())
    }
    var externalBuckName by remember { mutableStateOf(existingEvent?.externalBuckName ?: "") }
    val externalBuckBreedRows = remember {
        mutableStateListOf<BreedCompositionRow>().apply { addAll(existingEvent?.externalBuckBreedComposition?.toRows() ?: emptyList()) }
    }
    var breedPickerRowId by remember { mutableStateOf<String?>(null) }

    // Si se abre el diálogo para registrar un parto nuevo (no una edición) y la monta más
    // reciente de esta cabra ya trae semental cargado, se usa tal cual — no se vuelve a pedir.
    val loadedBuckEvent = linkedBreedingEvent?.takeIf {
        eventType == ReproductiveEventType.BIRTH &&
            existingEvent == null &&
            (it.buckId != null || !it.externalBuckName.isNullOrBlank())
    }
    val showEditableBuckSection = eventType == ReproductiveEventType.BREEDING ||
        (eventType == ReproductiveEventType.BIRTH && loadedBuckEvent == null)

    var result by remember { mutableStateOf(existingEvent?.result ?: ReproductiveEventResult.PENDING) }
    var kidsBorn by remember { mutableStateOf(existingEvent?.kidsBornCount?.toString() ?: "") }
    var kidsAlive by remember { mutableStateOf(existingEvent?.kidsAliveCount?.toString() ?: "") }
    var notes by remember { mutableStateOf(existingEvent?.notes ?: "") }
    var resultingDoeStatus by remember { mutableStateOf<GoatStatus?>(null) }
    val newKidNames = remember { mutableStateListOf<String>() }
    val newKidTags = remember { mutableStateListOf<String>() }
    val newKidSexes = remember { mutableStateListOf<GoatSex>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (existingEvent != null) Res.string.admin_repro_event_edit_dialog_title
                    else Res.string.admin_repro_event_dialog_title
                )
            )
        },
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
                            label = { Text(type.label()) }
                        )
                    }
                }

                if (showEditableBuckSection) {
                    Text(stringResource(Res.string.admin_repro_event_buck_label))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = !buckIsExternal,
                            onClick = { buckIsExternal = false; externalBuckName = ""; externalBuckBreedRows.clear() },
                            label = { Text(stringResource(Res.string.admin_goat_form_father_internal_label)) }
                        )
                        FilterChip(
                            selected = buckIsExternal,
                            onClick = { buckIsExternal = true; buckId = null },
                            label = { Text(stringResource(Res.string.admin_goat_form_father_external_label)) }
                        )
                    }
                    if (buckIsExternal) {
                        OutlinedTextField(
                            value = externalBuckName,
                            onValueChange = { externalBuckName = it },
                            label = { Text(stringResource(Res.string.admin_repro_event_external_buck_name_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            stringResource(Res.string.admin_goat_form_external_father_breed_label),
                            style = MaterialTheme.typography.bodySmall
                        )
                        BreedCompositionRowsEditor(
                            rows = externalBuckBreedRows,
                            onAddRow = { externalBuckBreedRows.add(BreedCompositionRow()) },
                            onRemoveRow = { rowId -> externalBuckBreedRows.removeAll { it.rowId == rowId } },
                            onPercentageChanged = { rowId, value ->
                                val index = externalBuckBreedRows.indexOfFirst { it.rowId == rowId }
                                if (index >= 0) externalBuckBreedRows[index] = externalBuckBreedRows[index].copy(percentageText = value)
                            },
                            onPickBreed = { rowId -> breedPickerRowId = rowId }
                        )
                    } else {
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
                } else if (loadedBuckEvent != null) {
                    Text(stringResource(Res.string.admin_repro_event_buck_from_breeding_label))
                    Text(linkedBuckSummary(loadedBuckEvent, bucks), style = MaterialTheme.typography.bodyMedium)
                }

                Text(stringResource(Res.string.admin_repro_event_result_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ReproductiveEventResult.entries) { r ->
                        FilterChip(
                            selected = result == r,
                            onClick = { result = r },
                            label = { Text(r.label()) }
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
                        if (existingEvent == null) {
                            Text(stringResource(Res.string.admin_repro_event_kid_records_label))
                            newKidNames.indices.forEach { index ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            stringResource(Res.string.admin_repro_event_kid_number_label, index + 1),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        IconButton(onClick = {
                                            newKidNames.removeAt(index)
                                            newKidTags.removeAt(index)
                                            newKidSexes.removeAt(index)
                                        }) {
                                            Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.admin_repro_event_remove_kid_content_description))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = newKidNames[index],
                                        onValueChange = { newKidNames[index] = it },
                                        label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = newKidTags[index],
                                        onValueChange = { newKidTags[index] = it },
                                        label = { Text(stringResource(Res.string.admin_repro_event_kid_tag_label)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = newKidSexes[index] == GoatSex.FEMALE,
                                            onClick = { newKidSexes[index] = GoatSex.FEMALE },
                                            label = { Text(stringResource(Res.string.admin_repro_event_kid_sex_female_short)) }
                                        )
                                        FilterChip(
                                            selected = newKidSexes[index] == GoatSex.MALE,
                                            onClick = { newKidSexes[index] = GoatSex.MALE },
                                            label = { Text(stringResource(Res.string.admin_repro_event_kid_sex_male_short)) }
                                        )
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
                        } else {
                            Text(
                                stringResource(Res.string.admin_repro_event_kids_already_registered_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
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
                val effectiveBuckId: String?
                val effectiveExternalBuckName: String?
                val effectiveExternalBuckBreedComposition: List<BreedPercentage>?
                if (loadedBuckEvent != null) {
                    effectiveBuckId = loadedBuckEvent.buckId
                    effectiveExternalBuckName = loadedBuckEvent.externalBuckName
                    effectiveExternalBuckBreedComposition = loadedBuckEvent.externalBuckBreedComposition
                } else {
                    effectiveBuckId = if (buckIsExternal) null else buckId
                    effectiveExternalBuckName = if (buckIsExternal) externalBuckName.trim().ifBlank { null } else null
                    effectiveExternalBuckBreedComposition = if (buckIsExternal) externalBuckBreedRows.toBreedComposition().ifEmpty { null } else null
                }
                onSave(
                    ReproductiveEventFormResult(
                        date = date,
                        eventType = eventType,
                        buckId = effectiveBuckId,
                        externalBuckName = effectiveExternalBuckName,
                        externalBuckBreedComposition = effectiveExternalBuckBreedComposition,
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

    breedPickerRowId?.let { rowId ->
        BreedPickerDialog(
            breeds = breeds,
            onDismiss = { breedPickerRowId = null },
            onSelect = { breed ->
                val index = externalBuckBreedRows.indexOfFirst { it.rowId == rowId }
                if (index >= 0) externalBuckBreedRows[index] = externalBuckBreedRows[index].copy(breedName = breed.name)
                breedPickerRowId = null
            }
        )
    }
}
