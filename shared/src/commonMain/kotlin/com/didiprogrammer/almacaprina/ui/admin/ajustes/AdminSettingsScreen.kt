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
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_mas_ajustes_label
import almacaprina.shared.generated.resources.admin_settings_change_pin_button
import almacaprina.shared.generated.resources.admin_settings_cost_per_liter_hint
import almacaprina.shared.generated.resources.admin_settings_cost_per_liter_label
import almacaprina.shared.generated.resources.admin_settings_costs_section_title
import almacaprina.shared.generated.resources.admin_settings_create_pin_button
import almacaprina.shared.generated.resources.admin_settings_currency_label
import almacaprina.shared.generated.resources.admin_settings_delete_button
import almacaprina.shared.generated.resources.admin_settings_deposit_alert_days_label
import almacaprina.shared.generated.resources.admin_settings_farm_name_label
import almacaprina.shared.generated.resources.admin_settings_farm_section_title
import almacaprina.shared.generated.resources.admin_settings_insufficient_data_fallback
import almacaprina.shared.generated.resources.admin_settings_logout_confirm_message
import almacaprina.shared.generated.resources.admin_settings_new_pin_label
import almacaprina.shared.generated.resources.admin_settings_pin_configured
import almacaprina.shared.generated.resources.admin_settings_pin_not_configured
import almacaprina.shared.generated.resources.admin_settings_pin_section_title
import almacaprina.shared.generated.resources.admin_settings_remove_pin_confirm_message
import almacaprina.shared.generated.resources.admin_settings_remove_pin_confirm_title
import almacaprina.shared.generated.resources.admin_settings_session_section_title
import almacaprina.shared.generated.resources.admin_settings_target_liters_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_logout_confirm_button
import almacaprina.shared.generated.resources.common_logout_confirm_title
import almacaprina.shared.generated.resources.common_save_button
import almacaprina.shared.generated.resources.common_saving_button
import almacaprina.shared.generated.resources.pin_setup_confirm_label
import almacaprina.shared.generated.resources.pin_setup_error_mismatch
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.FormField
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.SecondaryButton
import com.didiprogrammer.almacaprina.ui.components.SectionHeader
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
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
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.admin_mas_ajustes_label)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { SectionHeader(title = stringResource(Res.string.admin_settings_farm_section_title)) }
            item {
                AlmacaprinaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        FormField(
                            label = stringResource(Res.string.admin_settings_farm_name_label),
                            value = uiState.farmName,
                            onValueChange = viewModel::onFarmNameChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = stringResource(Res.string.admin_settings_currency_label),
                            value = uiState.currency,
                            onValueChange = viewModel::onCurrencyChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = stringResource(Res.string.admin_settings_target_liters_label),
                            value = uiState.targetDailyLitersGoalText,
                            onValueChange = viewModel::onTargetDailyLitersGoalChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FormField(
                            label = stringResource(Res.string.admin_settings_deposit_alert_days_label),
                            value = uiState.depositAlertDaysText,
                            onValueChange = viewModel::onDepositAlertDaysChanged,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PrimaryButton(
                            text = stringResource(if (uiState.isSaving) Res.string.common_saving_button else Res.string.common_save_button),
                            enabled = uiState.isValid && !uiState.isSaving,
                            loading = uiState.isSaving,
                            onClick = viewModel::save,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { SectionHeader(title = stringResource(Res.string.admin_settings_costs_section_title)) }
            item {
                AlmacaprinaCard {
                    Text(stringResource(Res.string.admin_settings_cost_per_liter_label), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                    Text(
                        uiState.costPerLiter?.let { formatCurrency(it, uiState.currency) } ?: stringResource(Res.string.admin_settings_insufficient_data_fallback),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(Res.string.admin_settings_cost_per_liter_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }
            }

            item { SectionHeader(title = stringResource(Res.string.admin_settings_pin_section_title)) }
            item {
                AlmacaprinaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Text(
                            stringResource(if (uiState.hasPin) Res.string.admin_settings_pin_configured else Res.string.admin_settings_pin_not_configured),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            SecondaryButton(
                                text = stringResource(if (uiState.hasPin) Res.string.admin_settings_change_pin_button else Res.string.admin_settings_create_pin_button),
                                onClick = { showPinDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.hasPin) {
                                SecondaryButton(
                                    text = stringResource(Res.string.admin_settings_delete_button),
                                    onClick = { showRemovePinConfirm = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader(title = stringResource(Res.string.admin_settings_session_section_title)) }
            item {
                AlmacaprinaCard {
                    TextButton(onClick = { showLogoutConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.common_logout_confirm_button), color = MaterialTheme.colorScheme.error)
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
            title = { Text(stringResource(Res.string.admin_settings_remove_pin_confirm_title)) },
            text = { Text(stringResource(Res.string.admin_settings_remove_pin_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onRemovePin()
                    showRemovePinConfirm = false
                }) { Text(stringResource(Res.string.admin_settings_delete_button)) }
            },
            dismissButton = { TextButton(onClick = { showRemovePinConfirm = false }) { Text(stringResource(Res.string.common_cancel)) } }
        )
    }
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(stringResource(Res.string.common_logout_confirm_title)) },
            text = { Text(stringResource(Res.string.admin_settings_logout_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout(onLoggedOut)
                }) { Text(stringResource(Res.string.common_logout_confirm_button)) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false } ) { Text(stringResource(Res.string.common_cancel)) } }
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
        title = { Text(stringResource(Res.string.admin_settings_pin_section_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                FormField(
                    label = stringResource(Res.string.admin_settings_new_pin_label),
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { pin = it; errorMessage = null } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation()
                )
                FormField(
                    label = stringResource(Res.string.pin_setup_confirm_label),
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
            val mismatchError = stringResource(Res.string.pin_setup_error_mismatch)
            TextButton(
                enabled = pin.length == 4 && confirmPin.length == 4,
                onClick = {
                    if (pin != confirmPin) errorMessage = mismatchError else onSave(pin)
                }
            ) { Text(stringResource(Res.string.common_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } }
    )
}
