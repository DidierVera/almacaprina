package com.didiprogrammer.almacaprina.ui.ventas.pendientes

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.AmbarBorde
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.Fondo
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Ventas · Cobrar pendientes (detalle por cliente). Ver mockup Ventas-selection-details.png. */
@Composable
fun PendingSalesDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    viewModel: PendingSalesDetailViewModel = koinViewModel(parameters = { parametersOf(customerId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(color = Fondo) {
                    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xxl), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            PaymentMethod.entries.forEach { method ->
                                val selected = uiState.paymentMethod == method
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { viewModel.onPaymentMethodSelected(method) },
                                    shape = ShapeLarge,
                                    color = if (selected) Verde else com.didiprogrammer.almacaprina.ui.theme.Superficie,
                                    border = BorderStroke(1.dp, if (selected) Verde else Borde)
                                ) {
                                    Text(
                                        method.label(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) SobreVerde else Tinta,
                                        modifier = Modifier.padding(vertical = Spacing.lg, horizontal = Spacing.sm),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        PrimaryButton(
                            text = if (uiState.isSaving) "Guardando…" else "Marcar todo como pagado",
                            enabled = !uiState.isSaving,
                            loading = uiState.isSaving,
                            onClick = viewModel::markAllPaid,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item { ScreenHeaderWithBack(eyebrow = null, title = uiState.customerName, onBack = onBack) }

                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = AmbarFondo, border = BorderStroke(1.dp, AmbarBorde)) {
                        Column(modifier = Modifier.padding(Spacing.xl)) {
                            Text("Saldo pendiente", style = MaterialTheme.typography.labelLarge, color = AmbarTexto)
                            Text(formatCurrency(uiState.totalPending, uiState.currency), style = MaterialTheme.typography.displaySmall, color = Tinta)
                            Text(
                                "${uiState.items.size} ${if (uiState.items.size == 1) "venta" else "ventas"} · la más antigua hace ${uiState.daysSinceOldest} días",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmbarTexto
                            )
                        }
                    }
                }

                item { Text("Ventas pendientes", style = MaterialTheme.typography.titleSmall, color = Tinta) }

                items(uiState.items, key = { it.saleId }) { item ->
                    AlmacaprinaCard {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.date.toString(), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                Text(item.description, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                            }
                            Text(
                                formatCurrency(item.amount, uiState.currency),
                                style = MaterialTheme.typography.titleSmall,
                                color = Tinta,
                                modifier = Modifier.padding(horizontal = Spacing.md)
                            )
                            OutlinedButton(onClick = { viewModel.markSalePaid(item.saleId) }, enabled = !uiState.isSaving) {
                                Text("Marcar\npagada", style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
