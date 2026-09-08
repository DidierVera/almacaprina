package com.didiprogrammer.almacaprina.ui.ventas.envases

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.deposits_detail_confirm_button
import almacaprina.shared.generated.resources.deposits_detail_refund_label
import almacaprina.shared.generated.resources.deposits_detail_return_quantity_label
import almacaprina.shared.generated.resources.deposits_detail_units_out_label
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Ventas · Devolver envase (detalle por cliente). */
@Composable
fun VentasDepositsDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    viewModel: VentasDepositsDetailViewModel = koinViewModel(parameters = { parametersOf(customerId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item { ScreenHeaderWithBack(eyebrow = null, title = uiState.customerName, onBack = onBack) }

                items(uiState.rows, key = { it.packagingId }) { row ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(row.packagingName, style = MaterialTheme.typography.titleSmall, color = Tinta)
                            Text(
                                stringResource(Res.string.deposits_detail_units_out_label, row.unitsOut, formatCurrency(row.depositAmountPerUnit, uiState.currency)),
                                style = MaterialTheme.typography.bodySmall,
                                color = TintaSuave
                            )
                            OutlinedTextField(
                                value = row.returnQuantityText,
                                onValueChange = { viewModel.onReturnQuantityChanged(row.packagingId, it) },
                                label = { Text(stringResource(Res.string.deposits_detail_return_quantity_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(Res.string.deposits_detail_refund_label), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                                Text(formatCurrency(row.refundAmount, uiState.currency), style = MaterialTheme.typography.titleSmall, color = Tinta)
                            }
                            PrimaryButton(
                                text = stringResource(Res.string.deposits_detail_confirm_button),
                                enabled = row.returnQuantity != null && !uiState.isSaving,
                                loading = uiState.isSaving,
                                onClick = { viewModel.confirmReturn(row.packagingId) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
