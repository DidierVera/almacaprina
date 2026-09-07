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
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.label
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
        topBar = { TopAppBar(title = { Text("Nueva compra") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Categoría")
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
                        Text("Envase del catálogo")
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
                        Text("Insumo del catálogo")
                        if (uiState.filteredInsumos.isEmpty()) {
                            Text(
                                "Sin insumos de esta categoría. Crea uno en Catálogo > Insumos.",
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
            item { DateField(label = "Fecha", date = uiState.date, onDateSelected = viewModel::onDateChanged) }
            item {
                OutlinedTextField(
                    value = uiState.supplier,
                    onValueChange = viewModel::onSupplierChanged,
                    label = { Text("Proveedor (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (uiState.hasPackageOption) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(checked = uiState.purchaseByPackage, onCheckedChange = viewModel::onPurchaseByPackageToggled)
                        val packageLabel = uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: "empaque"
                        val size = uiState.packageSize ?: 0.0
                        val unit = uiState.selectedInsumo?.unitOfMeasure?.label().orEmpty()
                        Text("Comprar por $packageLabel (${formatQuantity(size)} $unit c/u)")
                    }
                }
            }
            if (uiState.purchaseByPackage && uiState.hasPackageOption) {
                item {
                    OutlinedTextField(
                        value = uiState.packageCountText,
                        onValueChange = viewModel::onPackageCountChanged,
                        label = { Text("N° de ${(uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: "empaques").lowercase()}s") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.packageCostText,
                        onValueChange = viewModel::onPackageCostChanged,
                        label = { Text("Costo por ${(uiState.selectedInsumo?.purchasePackageLabel?.ifBlank { null } ?: "empaque").lowercase()}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (uiState.quantity != null && uiState.unitCost != null) {
                    item {
                        Text(
                            "= ${formatQuantity(uiState.quantity!!)} ${uiState.selectedInsumo?.unitOfMeasure?.label().orEmpty()} " +
                                "a ${formatCurrency(uiState.unitCost!!, uiState.currency)} c/u",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = uiState.quantityText,
                        onValueChange = viewModel::onQuantityChanged,
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.unitCostText,
                        onValueChange = viewModel::onUnitCostChanged,
                        label = { Text("Costo unitario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                AlmacaprinaCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total", style = MaterialTheme.typography.titleSmall)
                        Text(formatCurrency(uiState.totalCost, uiState.currency), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) else Text("Guardar")
                    }
                }
            }
        }
    }
}
