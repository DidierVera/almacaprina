package com.didiprogrammer.almacaprina.ui.admin.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.SecondaryButton
import com.didiprogrammer.almacaprina.ui.components.SectionHeader
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.koin.compose.viewmodel.koinViewModel

/** Sección "Más · Ajustes". Ver CLAUDE.md § Configuración. */
@Composable
fun AdminSettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: AdminSettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }
    var showRemovePinConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        (uiState.errorMessage ?: uiState.successMessage)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { SectionHeader(title = "Finca") }
            item {
                AlmacaprinaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        FormField(
                            label = "Nombre de la finca",
                            value = uiState.farmName,
                            onValueChange = viewModel::onFarmNameChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = "Moneda",
                            value = uiState.currency,
                            onValueChange = viewModel::onCurrencyChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = "Meta de producción (litros/día)",
                            value = uiState.targetDailyLitersGoalText,
                            onValueChange = viewModel::onTargetDailyLitersGoalChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = "Días de alerta por depósito de envase sin devolver",
                            value = uiState.depositAlertDaysText,
                            onValueChange = viewModel::onDepositAlertDaysChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
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

            item { SectionHeader(title = "Costos") }
            item {
                AlmacaprinaCard {
                    Text("Costo por litro (últimos 30 días)", style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                    Text(
                        uiState.costPerLiter?.let { formatCurrency(it, uiState.currency) } ?: "Sin datos suficientes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Se calcula solo, a partir de compras, salud y alimentación — no es editable aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }
            }

            item { SectionHeader(title = "PIN de acceso rápido") }
            item {
                AlmacaprinaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Text(
                            if (uiState.hasPin) "PIN configurado en este dispositivo" else "Sin PIN configurado",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            SecondaryButton(
                                text = if (uiState.hasPin) "Cambiar PIN" else "Crear PIN",
                                onClick = { showPinDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.hasPin) {
                                SecondaryButton(
                                    text = "Eliminar",
                                    onClick = { showRemovePinConfirm = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader(title = "Sesión") }
            item {
                AlmacaprinaCard {
                    TextButton(onClick = { showLogoutConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onSave = { pin ->
                viewModel.onSetPin(pin)
                showPinDialog = false
            }
        )
    }
    if (showRemovePinConfirm) {
        AlertDialog(
            onDismissRequest = { showRemovePinConfirm = false },
            title = { Text("¿Eliminar el PIN?") },
            text = { Text("La próxima vez que abras la app vas a necesitar tu correo y contraseña.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onRemovePin()
                    showRemovePinConfirm = false
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showRemovePinConfirm = false }) { Text("Cancelar") } }
        )
    }
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Vas a necesitar tu correo y contraseña para volver a entrar.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout(onLoggedOut)
                }) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false } ) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SetPinDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PIN de acceso rápido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                FormField(
                    label = "PIN nuevo (4 dígitos)",
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { pin = it; errorMessage = null } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation()
                )
                FormField(
                    label = "Confirma el PIN",
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { confirmPin = it; errorMessage = null } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation()
                )
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = pin.length == 4 && confirmPin.length == 4,
                onClick = {
                    if (pin != confirmPin) errorMessage = "Los PIN no coinciden." else onSave(pin)
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
