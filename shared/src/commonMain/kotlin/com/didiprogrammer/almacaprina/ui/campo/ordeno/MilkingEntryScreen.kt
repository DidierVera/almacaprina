package com.didiprogrammer.almacaprina.ui.campo.ordeno

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.milking_entry_back_button
import almacaprina.shared.generated.resources.milking_entry_editing_badge
import almacaprina.shared.generated.resources.milking_entry_lactation_suffix
import almacaprina.shared.generated.resources.milking_entry_liters_label
import almacaprina.shared.generated.resources.milking_entry_mark_unmilked_button
import almacaprina.shared.generated.resources.milking_entry_reason_dialog_goat_fallback
import almacaprina.shared.generated.resources.milking_entry_reason_dialog_message
import almacaprina.shared.generated.resources.milking_entry_reason_dialog_title
import almacaprina.shared.generated.resources.milking_entry_reason_dry
import almacaprina.shared.generated.resources.milking_entry_reason_other
import almacaprina.shared.generated.resources.milking_entry_reason_sick
import almacaprina.shared.generated.resources.milking_entry_reason_under_treatment
import almacaprina.shared.generated.resources.milking_entry_save_button
import almacaprina.shared.generated.resources.milking_entry_save_correction_button
import almacaprina.shared.generated.resources.milking_entry_step_label
import almacaprina.shared.generated.resources.milking_entry_use_keypad_button
import almacaprina.shared.generated.resources.milking_entry_use_stepper_button
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.NoMilkingReason
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import org.jetbrains.compose.resources.stringResource

private const val LITER_STEP = 0.1

/** Campo · Ordeño — registro individual. Ver mockups campo-Registrar ordeño-selection-registering-*.png. */
@Composable
fun MilkingEntryScreen(
    goatId: String,
    viewModel: MilkingSessionViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entry = uiState.selectedEntry

    LaunchedEffect(goatId) { viewModel.onSelectGoat(goatId) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(Spacing.xxl)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text(stringResource(Res.string.milking_entry_back_button)) }
                if (entry?.registered == true) {
                    Surface(shape = ShapeLarge, color = com.didiprogrammer.almacaprina.ui.theme.AmbarFondo) {
                        Text(
                            stringResource(Res.string.milking_entry_editing_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = com.didiprogrammer.almacaprina.ui.theme.AmbarTexto,
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))
            Text(entry?.goat?.name ?: "", style = MaterialTheme.typography.displaySmall, color = Tinta)
            Text(
                "${entry?.goat?.tagNumber ?: ""} · ${stringResource(Res.string.milking_entry_lactation_suffix, entry?.lactationNumber ?: 0)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )

            Spacer(Modifier.height(Spacing.xxl))
            Text(
                stringResource(Res.string.milking_entry_liters_label),
                style = MaterialTheme.typography.labelMedium,
                color = TintaSuave,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                Text(uiState.currentLitersText, style = MaterialTheme.typography.displayLarge, color = Tinta)
                Text(" L", style = MaterialTheme.typography.titleLarge, color = TintaSuave)
            }

            Spacer(Modifier.height(Spacing.xl))

            if (uiState.useNumericKeypad) {
                NumericKeypad(
                    onDigit = { digit -> viewModel.onLitersChanged((uiState.currentLitersText.takeIf { it != "0" } ?: "") + digit) },
                    onComma = {
                        if (!uiState.currentLitersText.contains(',')) viewModel.onLitersChanged(uiState.currentLitersText + ",")
                    },
                    onBackspace = {
                        val newText = uiState.currentLitersText.dropLast(1)
                        viewModel.onLitersChanged(newText.ifEmpty { "0" })
                    }
                )
                TextButton(onClick = { viewModel.onToggleKeypad(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.milking_entry_use_stepper_button), color = Terracota)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StepperButton(label = "–", color = Borde, contentColor = Tinta) {
                        val newValue = (uiState.currentLitersValue - LITER_STEP).coerceAtLeast(0.0)
                        viewModel.onLitersChanged(formatStepper(newValue))
                    }
                    Text(stringResource(Res.string.milking_entry_step_label, LITER_STEP.toString()), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                    StepperButton(label = "+", color = Terracota, contentColor = SobreVerde) {
                        val newValue = uiState.currentLitersValue + LITER_STEP
                        viewModel.onLitersChanged(formatStepper(newValue))
                    }
                }
                TextButton(onClick = { viewModel.onToggleKeypad(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.milking_entry_use_keypad_button), color = Terracota)
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            TextButton(onClick = { viewModel.onShowReasonPicker(true) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.milking_entry_mark_unmilked_button), color = TintaSuave)
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = if (entry?.registered == true) stringResource(Res.string.milking_entry_save_correction_button) else stringResource(Res.string.milking_entry_save_button),
                enabled = !uiState.isSaving,
                loading = uiState.isSaving,
                onClick = { viewModel.saveCurrentEntry(onSaved) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (uiState.showReasonPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowReasonPicker(false) },
            title = { Text(stringResource(Res.string.milking_entry_reason_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        stringResource(
                            Res.string.milking_entry_reason_dialog_message,
                            entry?.goat?.name ?: stringResource(Res.string.milking_entry_reason_dialog_goat_fallback)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilterChip(selected = false, onClick = { viewModel.markUnmilked(NoMilkingReason.DRY, onSaved) }, label = { Text(stringResource(Res.string.milking_entry_reason_dry)) })
                        FilterChip(selected = false, onClick = { viewModel.markUnmilked(NoMilkingReason.SICK, onSaved) }, label = { Text(stringResource(Res.string.milking_entry_reason_sick)) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilterChip(selected = false, onClick = { viewModel.markUnmilked(NoMilkingReason.UNDER_TREATMENT, onSaved) }, label = { Text(stringResource(Res.string.milking_entry_reason_under_treatment)) })
                        FilterChip(selected = false, onClick = { viewModel.markUnmilked(NoMilkingReason.OTHER, onSaved) }, label = { Text(stringResource(Res.string.milking_entry_reason_other)) })
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { viewModel.onShowReasonPicker(false) }) { Text(stringResource(Res.string.common_cancel)) } }
        )
    }
}

private fun formatStepper(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString().replace('.', ',')
}

@Composable
private fun StepperButton(label: String, color: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Surface(
        shape = ShapeLarge,
        color = color,
        contentColor = contentColor,
        modifier = Modifier.size(72.dp).clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun NumericKeypad(onDigit: (String) -> Unit, onComma: () -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(",", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Surface(
                        shape = ShapeLarge,
                        color = Borde,
                        modifier = Modifier.weight(1f).height(64.dp).clickable {
                            when (key) {
                                "," -> onComma()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(key, style = MaterialTheme.typography.headlineSmall, color = Tinta)
                        }
                    }
                }
            }
        }
    }
}
