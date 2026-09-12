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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_production_history_all_filter
import almacaprina.shared.generated.resources.admin_production_history_empty_message
import almacaprina.shared.generated.resources.admin_production_history_filter_title
import almacaprina.shared.generated.resources.admin_production_history_liters_used_label
import almacaprina.shared.generated.resources.admin_production_history_new_batch_content_description
import almacaprina.shared.generated.resources.admin_production_history_responsible_label
import almacaprina.shared.generated.resources.admin_production_history_yield_label
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.ChipFilterRow
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
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
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_production_history_new_batch_content_description))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val productFilterOptions = remember(uiState.derivedProducts) { listOf(null) + uiState.derivedProducts.map { it.id } }
            val allProductsLabel = stringResource(Res.string.admin_production_history_all_filter)
            ChipFilterRow(
                options = productFilterOptions,
                selected = uiState.selectedProductId,
                onSelect = viewModel::onProductFilterSelected,
                label = { productId -> if (productId == null) allProductsLabel else uiState.derivedProducts.firstOrNull { it.id == productId }?.name.orEmpty() },
                pickerTitle = stringResource(Res.string.admin_production_history_filter_title),
                modifier = Modifier.padding(16.dp)
            )

            RefreshableContent(
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                if (uiState.filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.admin_production_history_empty_message), style = MaterialTheme.typography.bodyMedium)
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
                text = stringResource(Res.string.admin_production_history_yield_label, ratio?.let { roundTo1Decimal(it).toString() } ?: "—"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        ) {
            Text(stringResource(Res.string.admin_production_history_liters_used_label, roundTo1Decimal(item.batch.milkLitersUsed).toString()), style = MaterialTheme.typography.bodyMedium)
            Text("${item.batch.outputQuantity} ${item.productUnit?.label() ?: ""}", style = MaterialTheme.typography.bodyMedium)
        }
        item.batch.responsible?.let {
            Text(stringResource(Res.string.admin_production_history_responsible_label, it), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
