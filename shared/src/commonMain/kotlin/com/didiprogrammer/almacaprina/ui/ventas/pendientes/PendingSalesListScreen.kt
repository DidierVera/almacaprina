package com.didiprogrammer.almacaprina.ui.ventas.pendientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.ScreenHeaderWithBack
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.koin.compose.viewmodel.koinViewModel

/** Ventas · Cobrar pendientes (lista). Ver mockup Ventas-selection-pending.png. */
@Composable
fun PendingSalesListScreen(
    onBack: () -> Unit,
    onCustomerClick: (String) -> Unit,
    viewModel: PendingSalesListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    ScreenHeaderWithBack(
                        eyebrow = "Cobrar pendientes",
                        title = "${formatCurrency(uiState.totalPending, uiState.currency)} por cobrar",
                        onBack = onBack,
                        subtitle = "Deuda más antigua primero"
                    )
                }

                items(uiState.items, key = { it.customerId }) { item ->
                    AlmacaprinaCard(onClick = { onCustomerClick(item.customerId) }) {
                        Text(item.customerName, style = MaterialTheme.typography.titleSmall, color = Tinta)
                        Text(formatCurrency(item.totalPending, uiState.currency), style = MaterialTheme.typography.displaySmall, color = Tinta)
                        Text(
                            "${item.pendingSalesCount} ${if (item.pendingSalesCount == 1) "venta pendiente" else "ventas pendientes"} · hace ${item.daysSinceOldest} días",
                            style = MaterialTheme.typography.bodySmall,
                            color = Terracota
                        )
                    }
                }
            }
        }
    }
}
