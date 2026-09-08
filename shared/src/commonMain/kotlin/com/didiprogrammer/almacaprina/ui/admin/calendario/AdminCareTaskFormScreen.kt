package com.didiprogrammer.almacaprina.ui.admin.calendario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_care_task_active_label
import almacaprina.shared.generated.resources.admin_care_task_form_animal_group_label
import almacaprina.shared.generated.resources.admin_care_task_form_delete_confirm_message
import almacaprina.shared.generated.resources.admin_care_task_form_delete_confirm_title
import almacaprina.shared.generated.resources.admin_care_task_form_delete_content_description
import almacaprina.shared.generated.resources.admin_care_task_form_edit_title
import almacaprina.shared.generated.resources.admin_care_task_form_frequency_label
import almacaprina.shared.generated.resources.admin_care_task_form_no_insumos_message
import almacaprina.shared.generated.resources.admin_care_task_form_quantity_label
import almacaprina.shared.generated.resources.admin_care_task_form_time_of_day_label
import almacaprina.shared.generated.resources.admin_goat_form_name_label
import almacaprina.shared.generated.resources.admin_health_event_type_label
import almacaprina.shared.generated.resources.admin_home_new_task_action
import almacaprina.shared.generated.resources.admin_recipe_item_insumo_label
import almacaprina.shared.generated.resources.admin_settings_delete_button
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Sección 6, pantalla 6.2 — Nueva/editar tarea. */
@Composable
fun AdminCareTaskFormScreen(
    taskId: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AdminCareTaskFormViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (uiState.isEditing) Res.string.admin_care_task_form_edit_title else Res.string.admin_home_new_task_action)) },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(Res.string.admin_care_task_form_delete_content_description))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text(stringResource(Res.string.admin_goat_form_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_health_event_type_label))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CareTaskType.entries) { type ->
                            FilterChip(
                                selected = uiState.taskType == type,
                                onClick = { viewModel.onTaskTypeChanged(type) },
                                label = { Text(type.label()) }
                            )
                        }
                    }
                }
            }
            if (uiState.needsInsumo) {
                item {
                    Column {
                        Text(stringResource(Res.string.admin_recipe_item_insumo_label))
                        if (uiState.insumos.isEmpty()) {
                            Text(stringResource(Res.string.admin_care_task_form_no_insumos_message))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(uiState.insumos, key = { it.id }) { insumo ->
                                    FilterChip(
                                        selected = uiState.insumoId == insumo.id,
                                        onClick = {
                                            viewModel.onInsumoSelected(if (uiState.insumoId == insumo.id) null else insumo.id)
                                        },
                                        label = { Text(insumo.name) }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.quantityPerOccurrenceText,
                        onValueChange = viewModel::onQuantityChanged,
                        label = { Text(stringResource(Res.string.admin_care_task_form_quantity_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_care_task_form_frequency_label))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CareTaskFrequency.entries) { freq ->
                            FilterChip(
                                selected = uiState.frequency == freq,
                                onClick = { viewModel.onFrequencyChanged(freq) },
                                label = { Text(freq.label()) }
                            )
                        }
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_care_task_form_animal_group_label))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CareTaskAnimalGroup.entries) { group ->
                            FilterChip(
                                selected = uiState.animalGroup == group,
                                onClick = {
                                    viewModel.onAnimalGroupChanged(if (uiState.animalGroup == group) null else group)
                                },
                                label = { Text(group.label()) }
                            )
                        }
                    }
                }
            }
            item {
                Column {
                    Text(stringResource(Res.string.admin_care_task_form_time_of_day_label))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TimeOfDay.entries) { time ->
                            FilterChip(
                                selected = uiState.timeOfDay == time,
                                onClick = { viewModel.onTimeOfDayChanged(if (uiState.timeOfDay == time) null else time) },
                                label = { Text(time.label()) }
                            )
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.admin_care_task_active_label))
                    Switch(checked = uiState.active, onCheckedChange = viewModel::onActiveChanged)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) else Text(stringResource(Res.string.common_save_button))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.admin_care_task_form_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.admin_care_task_form_delete_confirm_message)) },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(onSaved)
                }) { Text(stringResource(Res.string.admin_settings_delete_button)) }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(Res.string.common_cancel)) } }
        )
    }
}
