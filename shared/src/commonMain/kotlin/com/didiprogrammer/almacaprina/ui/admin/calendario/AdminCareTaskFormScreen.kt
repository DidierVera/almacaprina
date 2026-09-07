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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay
import com.didiprogrammer.almacaprina.ui.components.label
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

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (uiState.isEditing) "Editar tarea" else "Nueva tarea") }) },
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
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Column {
                    Text("Tipo")
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
                        Text("Insumo")
                        if (uiState.insumos.isEmpty()) {
                            Text("Sin insumos en el catálogo. Crea uno en Catálogo > Insumos.")
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
                        label = { Text("Cantidad esperada por ocurrencia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                Column {
                    Text("Frecuencia")
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
                    Text("Grupo de animales (opcional)")
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
                    Text("Momento del día (opcional)")
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
                    Text("Activa")
                    Switch(checked = uiState.active, onCheckedChange = viewModel::onActiveChanged)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) else Text("Guardar")
                    }
                }
            }
        }
    }
}
