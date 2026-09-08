package com.didiprogrammer.almacaprina.ui.admin.compras

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_new_batch_total_label
import almacaprina.shared.generated.resources.admin_new_purchase_buy_by_package_label
import almacaprina.shared.generated.resources.admin_new_purchase_computed_summary_label
import almacaprina.shared.generated.resources.admin_new_purchase_cost_per_package_label
import almacaprina.shared.generated.resources.admin_new_purchase_insumo_catalog_label
import almacaprina.shared.generated.resources.admin_new_purchase_no_insumos_message
import almacaprina.shared.generated.resources.admin_new_purchase_package_count_label
import almacaprina.shared.generated.resources.admin_new_purchase_package_plural_fallback
import almacaprina.shared.generated.resources.admin_new_purchase_package_singular_fallback
import almacaprina.shared.generated.resources.admin_new_purchase_packaging_catalog_label
import almacaprina.shared.generated.resources.admin_new_purchase_quantity_label
import almacaprina.shared.generated.resources.admin_new_purchase_supplier_label
import almacaprina.shared.generated.resources.admin_new_purchase_title
import almacaprina.shared.generated.resources.admin_packaging_form_unit_cost_label
import almacaprina.shared.generated.resources.admin_product_form_category_label
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_save_button
import almacaprina.shared.generated.resources.weighing_entry_date_label
import almacaprina.shared.generated.resources.weighing_entry_notes_label
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Sección 5, pantalla 5.2 — Nueva compra. */
@Composable
fun AdminNewPurchaseScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AdminNewPurchaseViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.admin_new_purchase_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(stringResource(Res.string.admin_product_form_category_label))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PurchaseCategory.entries) { category ->
                            FilterChip(
                                selected = uiState.category == category,
                                onClick = { viewModel.onCategoryChanged(category) },
                                label = { Text(category.label()) }
                            )
                        }
                    }
                }
            }
            item {
                if (uiState.category == PurchaseCategory.PACKAGING) {
                    Column {
                        Text(stringResource(Res.string.admin_new_purchase_packaging_catalog_label))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(uiState.packagings, key = { it.id }) { packaging ->
                                FilterChip(
                                    selected = uiState.selectedPackagingId == packaging.id,
                                    onClick = { viewModel.onPackagingSelected(packaging.id) },
                                    label = { Text(packaging.name) }
                                )
                            }
                        }
                    }
                } else {
                    Column {
                        Text(stringResource(Res.string.admin_new_purchase_insumo_catalog_label))
                        if (uiState.filteredInsumos.isEmpty()) {
                            Text(
                                stringResource(Res.string.admin_new_purchase_no_insumos_message),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(uiState.filteredInsumos, key = { it.id }) { insumo ->
                                    FilterChip(
                                        selected = uiState.selectedInsumoId == insumo.id,
                                        onClick = { viewModel.onInsumoSelected(insumo.id) },
                                        label = { Text(insumo.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { DateField(label = stringResource(Res.string.weighing_entry_date_label), date = uiState.date, onDateSelected = viewModel::onDateChanged) }
            item {
                OutlinedTextField(
                    value = uiState.supplier,
                    onValueChange = viewModel::onSupplierChanged,
                    label = { Text(stringResource(Res.string.admin_new_purchase_supplier_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (uiState.hasPackageOption) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(checked = uiState.purchaseByPackage, onCheckedChange = viewModel::onPurchaseByPackageToggled)
                        val packageLabel = uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: stringResource(Res.string.admin_new_purchase_package_singular_fallback)
                        val size = uiState.packageSize ?: 0.0
                        val unit = uiState.selectedInsumo?.unitOfMeasure?.label().orEmpty()
                        Text(stringResource(Res.string.admin_new_purchase_buy_by_package_label, packageLabel, formatQuantity(size), unit))
                    }
                }
            }
            if (uiState.purchaseByPackage && uiState.hasPackageOption) {
                item {
                    OutlinedTextField(
                        value = uiState.packageCountText,
                        onValueChange = viewModel::onPackageCountChanged,
                        label = {
                            val label = (uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: stringResource(Res.string.admin_new_purchase_package_plural_fallback)).lowercase()
                            Text(stringResource(Res.string.admin_new_purchase_package_count_label, label) + "s")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.packageCostText,
                        onValueChange = viewModel::onPackageCostChanged,
                        label = {
                            val label = (uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: stringResource(Res.string.admin_new_purchase_package_singular_fallback)).lowercase()
                            Text(stringResource(Res.string.admin_new_purchase_cost_per_package_label, label))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (uiState.quantity != null && uiState.unitCost != null) {
                    item {
                        Text(
                            stringResource(
                                Res.string.admin_new_purchase_computed_summary_label,
                                formatQuantity(uiState.quantity!!),
                                uiState.selectedInsumo?.unitOfMeasure?.label().orEmpty(),
                                formatCurrency(uiState.unitCost!!, uiState.currency)
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = uiState.quantityText,
                        onValueChange = viewModel::onQuantityChanged,
                        label = { Text(stringResource(Res.string.admin_new_purchase_quantity_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.unitCostText,
                        onValueChange = viewModel::onUnitCostChanged,
                        label = { Text(stringResource(Res.string.admin_packaging_form_unit_cost_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                AlmacaprinaCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.admin_new_batch_total_label), style = MaterialTheme.typography.titleSmall)
                        Text(formatCurrency(uiState.totalCost, uiState.currency), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text(stringResource(Res.string.weighing_entry_notes_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text(stringResource(Res.string.common_cancel))
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) else Text(stringResource(Res.string.common_save_button))
                    }
                }
            }
        }
    }
}
