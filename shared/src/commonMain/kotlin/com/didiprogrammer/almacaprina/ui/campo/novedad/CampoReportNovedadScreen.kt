package com.didiprogrammer.almacaprina.ui.campo.novedad

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_health_event_description_label
import almacaprina.shared.generated.resources.admin_health_event_next_date_label
import almacaprina.shared.generated.resources.admin_health_event_type_label
import almacaprina.shared.generated.resources.campo_novedad_eyebrow
import almacaprina.shared.generated.resources.campo_novedad_goat_label
import almacaprina.shared.generated.resources.campo_novedad_pick_goat_title
import almacaprina.shared.generated.resources.campo_novedad_select_goat_placeholder
import almacaprina.shared.generated.resources.campo_novedad_title
import almacaprina.shared.generated.resources.common_save_button
import almacaprina.shared.generated.resources.common_saving_button
import almacaprina.shared.generated.resources.weighing_entry_date_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.GoatPickerDialog
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Campo · Reportar novedad. Ruta `campo/novedad`. */
@Composable
fun CampoReportNovedadScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CampoReportNovedadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGoatPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    ScreenHeaderWithBack(
                        eyebrow = stringResource(Res.string.campo_novedad_eyebrow),
                        title = stringResource(Res.string.campo_novedad_title),
                        onBack = onBack
                    )
                }
                item {
                    Column {
                        Text(stringResource(Res.string.campo_novedad_goat_label), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { showGoatPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(uiState.selectedGoat?.name ?: stringResource(Res.string.campo_novedad_select_goat_placeholder))
                        }
                    }
                }
                item {
                    DateField(label = stringResource(Res.string.weighing_entry_date_label), date = uiState.date, onDateSelected = viewModel::onDateChanged)
                }
                item {
                    Column {
                        Text(stringResource(Res.string.admin_health_event_type_label), style = MaterialTheme.typography.bodySmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            items(HealthRecordType.entries) { t ->
                                FilterChip(
                                    selected = uiState.type == t,
                                    onClick = { viewModel.onTypeChanged(t) },
                                    label = { Text(t.label()) }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label = { Text(stringResource(Res.string.admin_health_event_description_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    DateField(
                        label = stringResource(Res.string.admin_health_event_next_date_label),
                        date = uiState.nextSuggestedDate,
                        onDateSelected = viewModel::onNextSuggestedDateChanged
                    )
                }
                item {
                    PrimaryButton(
                        text = if (uiState.isSaving) stringResource(Res.string.common_saving_button) else stringResource(Res.string.common_save_button),
                        enabled = uiState.isValid && !uiState.isSaving,
                        loading = uiState.isSaving,
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showGoatPicker) {
        GoatPickerDialog(
            title = stringResource(Res.string.campo_novedad_pick_goat_title),
            candidates = uiState.allGoats,
            onDismiss = { showGoatPicker = false },
            onSelect = { goat ->
                goat?.let(viewModel::onGoatSelected)
                showGoatPicker = false
            }
        )
    }
}
