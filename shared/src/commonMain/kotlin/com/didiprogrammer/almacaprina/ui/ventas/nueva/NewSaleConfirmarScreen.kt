package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_saving_button
import almacaprina.shared.generated.resources.new_sale_confirmar_deposit_label
import almacaprina.shared.generated.resources.new_sale_confirmar_paid_now_subtitle
import almacaprina.shared.generated.resources.new_sale_confirmar_paid_now_title
import almacaprina.shared.generated.resources.new_sale_confirmar_payment_method_title
import almacaprina.shared.generated.resources.new_sale_confirmar_payment_status_title
import almacaprina.shared.generated.resources.new_sale_confirmar_pending_subtitle
import almacaprina.shared.generated.resources.new_sale_confirmar_pending_title
import almacaprina.shared.generated.resources.new_sale_confirmar_save_button
import almacaprina.shared.generated.resources.new_sale_confirmar_title
import almacaprina.shared.generated.resources.new_sale_confirmar_total_label
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import org.jetbrains.compose.resources.stringResource

/** Nueva venta · Paso 4 — Confirmar. Ver mockup Ventas-selection-confirm.png. */
@Composable
fun NewSaleConfirmarScreen(
    viewModel: NewSaleViewModel,
    onBack: () -> Unit,
    onEditLine: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(color = com.didiprogrammer.almacaprina.ui.theme.Fondo) {
                PrimaryButton(
                    text = if (uiState.isSaving) stringResource(Res.string.common_saving_button) else stringResource(Res.string.new_sale_confirmar_save_button),
                    enabled = !uiState.isSaving && uiState.cartLines.isNotEmpty(),
                    loading = uiState.isSaving,
                    onClick = { viewModel.save(onSaved) },
                    modifier = Modifier.fillMaxWidth().padding(Spacing.xxl)
                )
            }
        }
    ) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item { NewSaleStepHeader(step = 4, title = stringResource(Res.string.new_sale_confirmar_title), onBack = onBack) }

                item { Text(uiState.selectedCustomer?.name ?: "", style = MaterialTheme.typography.titleMedium, color = Tinta) }

                item {
                    CartLinesList(
                        lines = uiState.cartLines,
                        packagingsById = uiState.packagingsById,
                        currency = uiState.currency,
                        onEdit = { index ->
                            viewModel.editCartLine(index)
                            onEditLine()
                        },
                        onRemove = viewModel::removeCartLine
                    )
                }

                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            if (uiState.cartDepositTotal > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(Res.string.new_sale_confirmar_deposit_label), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                                    Text(formatCurrency(uiState.cartDepositTotal, uiState.currency), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                }
                                HorizontalDivider(color = Borde)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(Res.string.new_sale_confirmar_total_label), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                Text(formatCurrency(uiState.cartGrandTotal, uiState.currency), style = MaterialTheme.typography.headlineSmall, color = Tinta)
                            }
                        }
                    }
                }

                item { Text(stringResource(Res.string.new_sale_confirmar_payment_method_title), style = MaterialTheme.typography.titleSmall, color = Tinta) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        items(PaymentMethod.entries) { method ->
                            val selected = uiState.paymentMethod == method
                            Surface(
                                modifier = Modifier.clickable { viewModel.onPaymentMethodSelected(method) },
                                shape = ShapeLarge,
                                color = if (selected) Verde else Superficie,
                                border = BorderStroke(1.dp, if (selected) Verde else Borde)
                            ) {
                                Text(
                                    method.label(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) SobreVerde else Tinta,
                                    modifier = Modifier.padding(vertical = Spacing.lg, horizontal = Spacing.lg),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item { Text(stringResource(Res.string.new_sale_confirmar_payment_status_title), style = MaterialTheme.typography.titleSmall, color = Tinta) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        val paidSelected = uiState.paymentStatus == PaymentStatus.PAID
                        Surface(
                            modifier = Modifier.weight(1f).clickable { viewModel.onPaymentStatusSelected(PaymentStatus.PAID) },
                            shape = ShapeLarge,
                            color = if (paidSelected) Verde.copy(alpha = 0.12f) else Superficie,
                            border = BorderStroke(1.dp, if (paidSelected) Verde else Borde)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.lg)) {
                                Text(stringResource(Res.string.new_sale_confirmar_paid_now_title), style = MaterialTheme.typography.titleSmall, color = if (paidSelected) Verde else Tinta)
                                Text(stringResource(Res.string.new_sale_confirmar_paid_now_subtitle), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                            }
                        }
                        val pendingSelected = uiState.paymentStatus == PaymentStatus.PENDING
                        Surface(
                            modifier = Modifier.weight(1f).clickable { viewModel.onPaymentStatusSelected(PaymentStatus.PENDING) },
                            shape = ShapeLarge,
                            color = if (pendingSelected) AmbarFondo else Superficie,
                            border = BorderStroke(1.dp, if (pendingSelected) AmbarTexto else Borde)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.lg)) {
                                Text(stringResource(Res.string.new_sale_confirmar_pending_title), style = MaterialTheme.typography.titleSmall, color = if (pendingSelected) AmbarTexto else Tinta)
                                Text(stringResource(Res.string.new_sale_confirmar_pending_subtitle), style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                            }
                        }
                    }
                }
            }
        }
    }
}
