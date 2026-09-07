package com.didiprogrammer.almacaprina.ui.admin.produccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.roundTo1Decimal
import com.didiprogrammer.almacaprina.business.yieldRatio
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import org.koin.compose.viewmodel.koinViewModel

/** Sección 4, pantalla 4.1 — Historial de lotes de producción. */
@Composable
fun AdminProductionHistoryScreen(
    onNewBatchClick: () -> Unit,
    viewModel: AdminProductionHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewBatchClick) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo lote")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedProductId == null,
                        onClick = { viewModel.onProductFilterSelected(null) },
                        label = { Text("Todos") }
                    )
                }
                items(uiState.derivedProducts, key = { it.id }) { product ->
                    FilterChip(
                        selected = uiState.selectedProductId == product.id,
                        onClick = { viewModel.onProductFilterSelected(product.id) },
                        label = { Text(product.name) }
                    )
                }
            }

            RefreshableContent(
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                if (uiState.filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin lotes de producción registrados todavía.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.filteredItems, key = { it.batch.id }) { item ->
                            BatchRow(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchRow(item: BatchHistoryItem) {
    AlmacaprinaCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(item.productName, style = MaterialTheme.typography.titleSmall)
                Text(item.batch.date.toString(), style = MaterialTheme.typography.bodySmall)
            }
            val ratio = item.batch.yieldRatio()
            Text(
                text = "Rinde ${ratio?.let { roundTo1Decimal(it) } ?: "—"} L/unidad",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        ) {
            Text("${roundTo1Decimal(item.batch.milkLitersUsed)} L usados", style = MaterialTheme.typography.bodyMedium)
            Text("${item.batch.outputQuantity} ${item.productUnitLabel}", style = MaterialTheme.typography.bodyMedium)
        }
        item.batch.responsible?.let {
            Text("Responsable: $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
