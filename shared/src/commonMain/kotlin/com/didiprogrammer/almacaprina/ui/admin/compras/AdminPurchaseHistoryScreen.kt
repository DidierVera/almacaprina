package com.didiprogrammer.almacaprina.ui.admin.compras

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
import androidx.compose.material3.TextButton
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
import com.didiprogrammer.almacaprina.business.totalCost
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.DateField
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import org.koin.compose.viewmodel.koinViewModel

/** Sección 5, pantalla 5.1 — Historial de compras. */
@Composable
fun AdminPurchaseHistoryScreen(
    onNewPurchaseClick: () -> Unit,
    viewModel: AdminPurchaseHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Compras") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewPurchaseClick) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva compra")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("Todas") }
                    )
                }
                items(PurchaseCategory.entries) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.label()) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateField(
                    label = "Desde",
                    date = uiState.fromDate,
                    onDateSelected = { viewModel.onFromDateChanged(it) },
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = "Hasta",
                    date = uiState.toDate,
                    onDateSelected = { viewModel.onToDateChanged(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.fromDate != null || uiState.toDate != null) {
                TextButton(
                    onClick = { viewModel.onFromDateChanged(null); viewModel.onToDateChanged(null) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) { Text("Limpiar fechas") }
            }

            RefreshableContent(
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                if (uiState.filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin compras registradas para este filtro.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total del filtro", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    formatCurrency(uiState.totalAmount, uiState.currency),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        items(uiState.filteredItems, key = { it.purchase.id }) { item ->
                            PurchaseRow(item, uiState.currency)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseRow(item: PurchaseHistoryItem, currency: String) {
    AlmacaprinaCard {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(item.itemName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.purchase.category.label()} · ${item.purchase.date}",
                    style = MaterialTheme.typography.bodySmall
                )
                item.purchase.supplier?.let {
                    Text("Proveedor: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                formatCurrency(item.purchase.totalCost(), currency),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
