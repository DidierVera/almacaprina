package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_continue
import almacaprina.shared.generated.resources.new_sale_cantidad_default_title
import almacaprina.shared.generated.resources.new_sale_cantidad_default_unit_label
import almacaprina.shared.generated.resources.new_sale_cantidad_new_units_hint
import almacaprina.shared.generated.resources.new_sale_cantidad_new_units_label
import almacaprina.shared.generated.resources.new_sale_cantidad_packaging_deposit_suffix
import almacaprina.shared.generated.resources.new_sale_cantidad_packaging_hint
import almacaprina.shared.generated.resources.new_sale_cantidad_packaging_no_deposit
import almacaprina.shared.generated.resources.new_sale_cantidad_packaging_section_title
import almacaprina.shared.generated.resources.new_sale_cantidad_partial_total_label
import almacaprina.shared.generated.resources.new_sale_cantidad_unit_equivalence
import almacaprina.shared.generated.resources.new_sale_cart_section_title
import almacaprina.shared.generated.resources.new_sale_add_another_button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.jetbrains.compose.resources.stringResource

private const val QUANTITY_STEP = 1.0

/** Nueva venta · Paso 3 — Cantidad y envase. Ver mockup Ventas-selection-quantity.png. */
@Composable
fun NewSaleCantidadScreen(
    viewModel: NewSaleViewModel,
    onBack: () -> Unit,
    onAddAnotherProduct: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val product = uiState.selectedProduct

    Scaffold(
        bottomBar = {
            Surface(color = Fondo) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(Spacing.xxl)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(Res.string.new_sale_cantidad_partial_total_label), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        Text(
                            formatCurrency(uiState.cartGrandTotal + uiState.total, uiState.currency),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Tinta
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        OutlinedButton(
                            enabled = uiState.canContinueFromCantidad,
                            onClick = {
                                viewModel.addCurrentLineToCart()
                                onAddAnotherProduct()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(Res.string.new_sale_add_another_button)) }
                        PrimaryButton(
                            text = stringResource(Res.string.common_continue),
                            enabled = uiState.canContinueFromCantidad,
                            onClick = {
                                viewModel.addCurrentLineToCart()
                                onContinue()
                            },
                            modifier = Modifier.weight(1f)
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
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item {
                    NewSaleStepHeader(step = 3, title = uiState.selectedCustomer?.name ?: "", onBack = onBack)
                }

                if (uiState.cartLines.isNotEmpty()) {
                    item { Text(stringResource(Res.string.new_sale_cart_section_title), style = MaterialTheme.typography.titleSmall, color = Tinta) }
                    item {
                        CartLinesList(
                            lines = uiState.cartLines,
                            packagingsById = uiState.packagingsById,
                            currency = uiState.currency,
                            onEdit = { index ->
                                viewModel.editCartLine(index)
                                onAddAnotherProduct()
                            },
                            onRemove = viewModel::removeCartLine
                        )
                    }
                }

                item {
                    val unitLabel = product?.saleUnit?.label()?.lowercase() ?: stringResource(Res.string.new_sale_cantidad_default_unit_label)
                    Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(product?.saleUnit?.label() ?: stringResource(Res.string.new_sale_cantidad_default_title), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                if (product != null) {
                                    Text(
                                        stringResource(Res.string.new_sale_cantidad_unit_equivalence, unitLabel, formatCurrency(product.defaultUnitPrice, uiState.currency)),
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
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
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
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                    modifier = Modifier.size(56.dp)
                                ) { Text("+", style = MaterialTheme.typography.headlineSmall) }
                            }
                        }
                    }
                }

                item { Text(stringResource(Res.string.new_sale_cantidad_packaging_section_title), style = MaterialTheme.typography.titleSmall, color = Tinta) }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        uiState.availablePackagings.forEach { packaging ->
                            val selected = uiState.selectedPackagingId == packaging.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onPackagingSelected(packaging.id) },
                                shape = ShapeLarge,
                                color = if (selected) Verde.copy(alpha = 0.12f) else Superficie,
                                border = BorderStroke(1.dp, if (selected) Verde else Borde)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text(packaging.name, style = MaterialTheme.typography.titleSmall, color = if (selected) Verde else Tinta)
                                    Text(
                                        if (packaging.isReturnable) {
                                            stringResource(Res.string.new_sale_cantidad_packaging_deposit_suffix, formatCurrency(packaging.depositAmount ?: 0.0, uiState.currency))
                                        } else {
                                            stringResource(Res.string.new_sale_cantidad_packaging_no_deposit)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TintaSuave
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.selectedPackaging?.isReturnable == true) {
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = ShapeExtraLarge, color = Superficie, border = BorderStroke(1.dp, Borde)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(Res.string.new_sale_cantidad_new_units_label), style = MaterialTheme.typography.titleSmall, color = Tinta)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                        Button(
                                            onClick = { viewModel.onNewPackagingUnitsChanged(uiState.newPackagingUnitsCount - 1) },
                                            shape = ShapeLarge,
                                            colors = ButtonDefaults.buttonColors(containerColor = Borde, contentColor = Tinta),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                            modifier = Modifier.size(40.dp)
                                        ) { Text("–") }
                                        Text("${uiState.newPackagingUnitsCount}", style = MaterialTheme.typography.titleLarge, color = Tinta)
                                        Button(
                                            onClick = { viewModel.onNewPackagingUnitsChanged(uiState.newPackagingUnitsCount + 1) },
                                            shape = ShapeLarge,
                                            colors = ButtonDefaults.buttonColors(containerColor = Verde, contentColor = SobreVerde),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                            modifier = Modifier.size(40.dp)
                                        ) { Text("+") }
                                    }
                                }
                                Text(
                                    stringResource(Res.string.new_sale_cantidad_new_units_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TintaSuave,
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(Res.string.new_sale_cantidad_packaging_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }
            }
        }
    }
}
