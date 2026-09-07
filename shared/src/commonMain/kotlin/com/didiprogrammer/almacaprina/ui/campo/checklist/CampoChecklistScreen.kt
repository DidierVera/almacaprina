package com.didiprogrammer.almacaprina.ui.campo.checklist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.ChecklistCareTaskItem
import com.didiprogrammer.almacaprina.business.ChecklistHealthReminder
import com.didiprogrammer.almacaprina.business.formatLongSpanishDate
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshOnResume
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.StatusChip
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.Regla
import com.didiprogrammer.almacaprina.ui.theme.Riel
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaOff
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

/** Campo · Checklist de hoy (Home de Campo). Ver mockups Campo Checklist-selection*.png. */
@Composable
fun CampoChecklistScreen(
    onOpenOrdeno: () -> Unit,
    onOpenPesada: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: CampoChecklistViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        (uiState.errorMessage ?: uiState.successMessage)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding)
        ) {
            val checklist = uiState.checklist
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(formatLongSpanishDate(today), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                            Text("Checklist de hoy", style = MaterialTheme.typography.displaySmall, color = Tinta)
                        }
                        IconButton(onClick = { showLogoutConfirm = true }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = TintaSuave)
                        }
                    }
                }

                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${checklist.completedCount}", style = MaterialTheme.typography.displayMedium, color = Tinta)
                                Text(" de ${checklist.totalCount}", style = MaterialTheme.typography.titleMedium, color = TintaSuave)
                                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                                Text("tareas completadas", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                            }
                            LinearProgressIndicator(
                                progress = { if (checklist.totalCount > 0) checklist.completedCount.toFloat() / checklist.totalCount else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = Spacing.md),
                                color = Verde,
                                trackColor = Riel
                            )
                        }
                    }
                }

                if (checklist.milkingTask != null) {
                    item { Text("AHORA", style = MaterialTheme.typography.labelSmall, color = TintaSuave) }
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenOrdeno),
                            shape = com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge,
                            color = Superficie,
                            border = BorderStroke(1.dp, Borde)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatusChip(label = "ORDEÑO", containerColor = Riel, contentColor = Tinta)
                                    CompletionCircle(completed = checklist.milkingProgress.isComplete)
                                }
                                Text(
                                    checklist.milkingTask.careTask.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Tinta,
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                                Text(
                                    "${checklist.milkingProgress.totalCount} cabras en ordeño",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TintaSuave
                                )
                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md), color = Borde)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Abrir registro de ordeño", style = MaterialTheme.typography.labelLarge, color = Terracota)
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Terracota)
                                }
                            }
                        }
                    }
                }

                item { Text("RESTO DEL DÍA", style = MaterialTheme.typography.labelSmall, color = TintaSuave) }

                items(checklist.otherCareTasks) { item ->
                    CareTaskRow(
                        item = item,
                        insumoName = item.careTask.insumoId?.let { uiState.insumosById[it]?.name },
                        onClick = {
                            if (!item.completed) {
                                if (item.careTask.taskType == CareTaskType.WEIGHING) onOpenPesada() else viewModel.onCareTaskClicked(item.careTask)
                            }
                        }
                    )
                }

                items(checklist.healthReminders) { reminder ->
                    HealthReminderRow(
                        reminder = reminder,
                        onClick = { if (!reminder.completed) viewModel.onHealthReminderClicked(reminder.healthRecord) }
                    )
                }
            }
        }
    }

    val confirmingTask = uiState.confirmingCareTask
    if (confirmingTask != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissCareTaskConfirm,
            title = { Text(confirmingTask.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (confirmingTask.insumoId != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cantidad aplicada", style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                            Text("esperado ${confirmingTask.quantityPerOccurrence ?: 0.0}", style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                        }
                        OutlinedTextField(
                            value = uiState.confirmQuantityText,
                            onValueChange = viewModel::onConfirmQuantityChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        val diff = (uiState.confirmQuantityText.toDoubleOrNull() ?: 0.0) - (confirmingTask.quantityPerOccurrence ?: 0.0)
                        Text(
                            "Diferencia con lo esperado: ${if (diff == 0.0) "Sin diferencia" else diff.toString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TintaSuave
                        )
                    } else {
                        Text("¿Confirmar esta tarea como completada?", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmCareTask, enabled = !uiState.isSaving) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = viewModel::onDismissCareTaskConfirm) { Text("Cancelar") } }
        )
    }

    val confirmingReminder = uiState.confirmingHealthReminder
    if (confirmingReminder != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissHealthReminderConfirm,
            title = { Text(confirmingReminder.type.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Cantidad aplicada", style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                    OutlinedTextField(
                        value = uiState.confirmHealthQuantityText,
                        onValueChange = viewModel::onConfirmHealthQuantityChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmHealthReminder, enabled = !uiState.isSaving) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = viewModel::onDismissHealthReminderConfirm) { Text("Cancelar") } }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("¿Cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout(onLoggedOut)
                }) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CompletionCircle(completed: Boolean) {
    Surface(shape = CircleShape, color = if (completed) Verde else Superficie, border = BorderStroke(1.dp, if (completed) Verde else Borde), modifier = Modifier.size(28.dp)) {
        if (completed) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = SobreVerde, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun CareTaskType.chipColors(): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> = when (this) {
    CareTaskType.FEEDING -> Verde.copy(alpha = 0.15f) to Verde
    CareTaskType.MEDICATION -> Terracota.copy(alpha = 0.15f) to Terracota
    CareTaskType.MILKING, CareTaskType.WEIGHING, CareTaskType.OTHER -> Riel to Tinta
}

@Composable
private fun CareTaskRow(item: ChecklistCareTaskItem, insumoName: String?, onClick: () -> Unit) {
    val (chipBg, chipFg) = item.careTask.taskType.chipColors()
    val subtitle = when {
        insumoName != null && item.careTask.quantityPerOccurrence != null ->
            "$insumoName · ${item.careTask.quantityPerOccurrence}"
        else -> "Recordatorio · ${item.careTask.timeOfDay?.label() ?: "Hoy"}"
    }
    ChecklistRow(
        completed = item.completed,
        title = item.careTask.name,
        chipLabel = item.careTask.taskType.label(),
        chipBg = chipBg,
        chipFg = chipFg,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
private fun HealthReminderRow(reminder: ChecklistHealthReminder, onClick: () -> Unit) {
    val goatLabel = reminder.goat?.let { "${it.name} (${it.tagNumber})" } ?: "cabra eliminada"
    ChecklistRow(
        completed = reminder.completed,
        title = "${reminder.healthRecord.type.name.lowercase().replaceFirstChar { it.uppercase() }} a $goatLabel",
        chipLabel = "SALUD",
        chipBg = Terracota.copy(alpha = 0.15f),
        chipFg = Terracota,
        subtitle = reminder.healthRecord.dosage ?: "Recordatorio individual",
        onClick = onClick
    )
}

@Composable
private fun ChecklistRow(
    completed: Boolean,
    title: String,
    chipLabel: String,
    chipBg: androidx.compose.ui.graphics.Color,
    chipFg: androidx.compose.ui.graphics.Color,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !completed, onClick = onClick),
        shape = com.didiprogrammer.almacaprina.ui.theme.ShapeLarge,
        color = if (completed) Regla else Superficie,
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            CompletionCircle(completed = completed)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (completed) TintaOff else Tinta,
                    textDecoration = if (completed) TextDecoration.LineThrough else null
                )
                StatusChip(label = chipLabel, containerColor = chipBg, contentColor = chipFg, modifier = Modifier.padding(vertical = Spacing.xs))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
            }
        }
    }
}
