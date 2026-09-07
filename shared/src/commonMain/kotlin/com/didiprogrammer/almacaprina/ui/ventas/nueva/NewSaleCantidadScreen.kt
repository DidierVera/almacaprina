package com.didiprogrammer.almacaprina.ui.ventas.nueva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.BordeControl
import com.didiprogrammer.almacaprina.ui.theme.Fondo
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde

private const val QUANTITY_STEP = 1.0

/** Nueva venta · Paso 3 — Cantidad y envase. Ver mockup Ventas-selection-quantity.png. */
@Composable
fun NewSaleCantidadScreen(
    viewModel: NewSaleViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val product = uiState.selectedProduct

    Scaffold(
        bottomBar = {
            Surface(color = Fondo) {
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xxl)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total parcial", style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        Text(formatCurrency(uiState.total, uiState.currency), style = MaterialTheme.typography.headlineSmall, color = Tinta)
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.md))
                    PrimaryButton(
                        text = "Continuar",
                        enabled = uiState.canContinueFromCantidad,
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item {
                    NewSaleStepHeader(step = 3, title = uiState.selectedCustomer?.name ?: "", onBack = onBack)
                }

                item {
                    val unitLabel = product?.saleUnit?.label()?.lowercase() ?: "unidades"
                    Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(product?.saleUnit?.label() ?: "Cantidad", style = MaterialTheme.typography.titleSmall, color = Tinta)
                                if (product != null) {
                                    Text(
                                        "1 unidad = 1 $unitLabel · ${formatCurrency(product.defaultUnitPrice, uiState.currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TintaSuave
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val newValue = ((uiState.quantity ?: 0.0) - QUANTITY_STEP).coerceAtLeast(0.0)
                                        viewModel.onQuantityChanged(formatQuantity(newValue))
                                    },
                                    shape = ShapeLarge,
                                    colors = ButtonDefaults.buttonColors(containerColor = Borde, contentColor = Tinta),
                                    modifier = Modifier.size(56.dp)
                                ) { Text("–", style = MaterialTheme.typography.headlineSmall) }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        uiState.quantityText.ifBlank { "0" },
                                        style = MaterialTheme.typography.displayMedium,
                                        color = Tinta
                                    )
                                    Text(unitLabel, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                                }

                                Button(
                                    onClick = {
                                        val newValue = (uiState.quantity ?: 0.0) + QUANTITY_STEP
                                        viewModel.onQuantityChanged(formatQuantity(newValue))
                                    },
                                    shape = ShapeLarge,
                                    colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = SobreVerde),
                                    modifier = Modifier.size(56.dp)
                                ) { Text("+", style = MaterialTheme.typography.headlineSmall) }
                            }
                        }
                    }
                }

                item { Text("Envase", style = MaterialTheme.typography.titleSmall, color = Tinta) }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        uiState.packagings.forEach { packaging ->
                            val selected = uiState.selectedPackagingId == packaging.id
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onPackagingSelected(packaging.id) },
                                shape = ShapeLarge,
                                color = if (selected) Verde.copy(alpha = 0.12f) else Superficie,
                                border = BorderStroke(1.dp, if (selected) Verde else Borde)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text(packaging.name, style = MaterialTheme.typography.titleSmall, color = if (selected) Verde else Tinta)
                                    Text(
                                        if (packaging.isReturnable) {
                                            "+${formatCurrency(packaging.depositAmount ?: 0.0, uiState.currency)} depósito c/u"
                                        } else {
                                            "Sin depósito"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TintaSuave
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Por ahora todos los productos pasan por el mismo selector de envase; " +
                            "no hay lógica distinta por producto en esta iteración.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }
            }
        }
    }
}
