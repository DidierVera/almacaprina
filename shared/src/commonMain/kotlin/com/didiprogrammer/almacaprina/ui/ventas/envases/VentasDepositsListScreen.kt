package com.didiprogrammer.almacaprina.ui.ventas.envases

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.deposits_list_empty_message
import almacaprina.shared.generated.resources.deposits_list_eyebrow
import almacaprina.shared.generated.resources.deposits_list_subtitle
import almacaprina.shared.generated.resources.deposits_list_title
import almacaprina.shared.generated.resources.deposits_list_units_out_suffix
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Ventas · Devolver envase (lista) — clientes con envases retornables sin devolver. */
@Composable
fun VentasDepositsListScreen(
    onBack: () -> Unit,
    onCustomerClick: (String) -> Unit,
    viewModel: VentasDepositsListViewModel = koinViewModel()
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
                        eyebrow = stringResource(Res.string.deposits_list_eyebrow),
                        title = stringResource(Res.string.deposits_list_title, uiState.totalUnitsOut),
                        onBack = onBack,
                        subtitle = stringResource(Res.string.deposits_list_subtitle, formatCurrency(uiState.totalDepositValue, uiState.currency))
                    )
                }

                if (uiState.items.isEmpty()) {
                    item {
                        Text(
                            stringResource(Res.string.deposits_list_empty_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TintaSuave
                        )
                    }
                }

                items(uiState.items, key = { it.customerId }) { item ->
                    AlmacaprinaCard(onClick = { onCustomerClick(item.customerId) }) {
                        Text(item.customerName, style = MaterialTheme.typography.titleSmall, color = Tinta)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(Res.string.deposits_list_units_out_suffix, item.totalUnitsOut),
                                style = MaterialTheme.typography.bodySmall,
                                color = TintaSuave
                            )
                            Text(formatCurrency(item.totalDepositValue, uiState.currency), style = MaterialTheme.typography.titleSmall, color = Tinta)
                        }
                    }
                }
            }
        }
    }
}
