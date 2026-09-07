package com.didiprogrammer.almacaprina.ui.campo.pesada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Campo · Registrar pesada. Ruta `campo/pesada/{goatId}`. */
@Composable
fun CampoWeighingEntryScreen(
    goatId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CampoWeighingEntryViewModel = koinViewModel(parameters = { parametersOf(goatId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                        eyebrow = "Registrar pesada",
                        title = uiState.goat?.name ?: "",
                        onBack = onBack,
                        subtitle = uiState.goat?.tagNumber?.let { "Arete $it" }
                    )
                }
                item {
                    DateField(label = "Fecha", date = uiState.date, onDateSelected = viewModel::onDateChanged)
                }
                item {
                    OutlinedTextField(
                        value = uiState.weightText,
                        onValueChange = viewModel::onWeightChanged,
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Column {
                        Text("Body Condition Score (1-5, opcional)", style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            (1..5).forEach { score ->
                                FilterChip(
                                    selected = uiState.bodyConditionScore == score,
                                    onClick = { viewModel.onBcsChanged(if (uiState.bodyConditionScore == score) null else score) },
                                    label = { Text(score.toString()) }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChanged,
                        label = { Text("Notas (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    PrimaryButton(
                        text = if (uiState.isSaving) "Guardando…" else "Guardar",
                        enabled = uiState.isValid && !uiState.isSaving,
                        loading = uiState.isSaving,
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
