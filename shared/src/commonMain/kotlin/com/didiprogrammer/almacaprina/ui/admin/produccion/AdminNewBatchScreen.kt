package com.didiprogrammer.almacaprina.ui.admin.produccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.didiprogrammer.almacaprina.business.roundTo1Decimal
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.DateField
import org.koin.compose.viewmodel.koinViewModel

/** Sección 4, pantalla 4.2 — Nuevo lote (flujo de varios pasos). */
@Composable
fun AdminNewBatchScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AdminNewBatchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo lote — ${uiState.step.title}") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { (uiState.step.ordinal + 1f) / BatchWizardStep.entries.size },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                when (uiState.step) {
                    BatchWizardStep.PRODUCTO -> ProductoStep(uiState.derivedProducts, uiState.selectedProduct, viewModel::onProductSelected)
                    BatchWizardStep.LITROS -> LitrosStep(uiState, viewModel::onMilkLitersChanged)
                    BatchWizardStep.CANTIDAD -> CantidadStep(uiState, viewModel::onOutputQuantityChanged)
                    BatchWizardStep.INSUMOS -> InsumosStep(uiState, viewModel::onInsumoQuantityChanged)
                    BatchWizardStep.RESPONSABLE -> ResponsableStep(
                        uiState,
                        viewModel::onDateChanged,
                        viewModel::onResponsibleChanged,
                        viewModel::onNotesChanged
                    )
                    BatchWizardStep.CONFIRMAR -> ConfirmarStep(uiState)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (uiState.step == BatchWizardStep.PRODUCTO) onCancel() else viewModel.goBack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.step == BatchWizardStep.PRODUCTO) "Cancelar" else "Atrás")
                }
                if (uiState.step == BatchWizardStep.CONFIRMAR) {
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.height(20.dp)) else Text("Guardar lote")
                    }
                } else {
                    Button(
                        onClick = viewModel::goNext,
                        enabled = uiState.canGoNext,
                        modifier = Modifier.weight(1f)
                    ) { Text("Siguiente") }
                }
            }
        }
    }
}

@Composable
private fun ProductoStep(products: List<Product>, selected: Product?, onSelect: (Product) -> Unit) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "No hay productos derivados activos. Crea uno en Catálogo > Productos.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(products, key = { it.id }) { product ->
            AlmacaprinaCard(onClick = { onSelect(product) }) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall)
                    if (selected?.id == product.id) {
                        Text("Seleccionado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LitrosStep(uiState: AdminNewBatchUiState, onChange: (Double) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("¿Cuántos litros de leche se usarán en este lote?", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledIconButton(onClick = { onChange((uiState.milkLitersUsed - 1).coerceAtLeast(0.0)) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Menos")
            }
            Text(
                text = "${roundTo1Decimal(uiState.milkLitersUsed)} L",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            FilledIconButton(onClick = { onChange(uiState.milkLitersUsed + 1) }) {
                Icon(Icons.Filled.Add, contentDescription = "Más")
            }
        }
        Text(
            "Litros disponibles: ${roundTo1Decimal(uiState.availableMilkLiters)} L",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        if (uiState.milkExceedsAvailable) {
            Text(
                "Vas a usar más litros de los que hay disponibles según el balance actual — se puede guardar igual, pero revisa el dato.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CantidadStep(uiState: AdminNewBatchUiState, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val unit = uiState.selectedProduct?.saleUnit
        Text("¿Cuánto se obtuvo de ${uiState.selectedProduct?.name ?: "producto"}?", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = uiState.outputQuantityText,
            onValueChange = onChange,
            label = { Text("Cantidad obtenida") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        val ratio = uiState.currentYieldRatio
        if (ratio != null) {
            Text(
                "Rendimiento: ${roundTo1Decimal(ratio)} L por unidad",
                style = MaterialTheme.typography.titleMedium
            )
            uiState.historicalAverageYield?.let { avg ->
                val diffLabel = when {
                    ratio > avg * 1.05 -> "por encima del promedio histórico (${roundTo1Decimal(avg)} L/unidad)"
                    ratio < avg * 0.95 -> "por debajo del promedio histórico (${roundTo1Decimal(avg)} L/unidad)"
                    else -> "en línea con el promedio histórico (${roundTo1Decimal(avg)} L/unidad)"
                }
                Text(diffLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun InsumosStep(uiState: AdminNewBatchUiState, onQuantityChanged: (String, Double) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.insumoUsages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Este producto no tiene receta configurada en Catálogo > Recetas, así que no hay insumos prellenados.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.insumoUsages, key = { it.insumoId }) { usage ->
                    AlmacaprinaCard {
                        Text(usage.insumoName, style = MaterialTheme.typography.titleSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = usage.quantityUsed.toString(),
                                onValueChange = { text -> text.toDoubleOrNull()?.let { onQuantityChanged(usage.insumoId, it) } },
                                label = { Text(usage.unitOfMeasureLabel) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Text(
                                formatCurrency(usage.cost, uiState.currency),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
        AlmacaprinaCard(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Costo total de insumos", style = MaterialTheme.typography.titleSmall)
                Text(formatCurrency(uiState.insumosCost, uiState.currency), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun ResponsableStep(
    uiState: AdminNewBatchUiState,
    onDateChanged: (kotlinx.datetime.LocalDate) -> Unit,
    onResponsibleChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DateField(label = "Fecha del lote", date = uiState.date, onDateSelected = onDateChanged)
        OutlinedTextField(
            value = uiState.responsible,
            onValueChange = onResponsibleChanged,
            label = { Text("Responsable") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChanged,
            label = { Text("Notas (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConfirmarStep(uiState: AdminNewBatchUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AlmacaprinaCard {
            SummaryRow("Fecha", uiState.date?.toString() ?: "")
            SummaryRow("Producto", uiState.selectedProduct?.name ?: "")
            SummaryRow("Litros usados", "${roundTo1Decimal(uiState.milkLitersUsed)} L")
            SummaryRow("Cantidad obtenida", "${uiState.outputQuantity ?: 0.0} ${uiState.selectedProduct?.saleUnit?.name ?: ""}")
            uiState.currentYieldRatio?.let { SummaryRow("Rendimiento", "${roundTo1Decimal(it)} L/unidad") }
            SummaryRow("Responsable", uiState.responsible.ifBlank { "Sin especificar" })
        }
        AlmacaprinaCard {
            Text("Costo del lote", style = MaterialTheme.typography.titleSmall)
            SummaryRow("Leche (costo por litro estimado)", formatCurrency(uiState.milkCost, uiState.currency))
            SummaryRow("Insumos", formatCurrency(uiState.insumosCost, uiState.currency))
            SummaryRow("Total", formatCurrency(uiState.totalCost, uiState.currency))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
