package com.didiprogrammer.almacaprina.ui.admin.home

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_home_alerts_title
import almacaprina.shared.generated.resources.admin_home_no_alerts_message
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.ui.components.AlertRow
import com.didiprogrammer.almacaprina.ui.components.RefreshOnResume
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Listado completo de alertas de Inicio ("Ver más") — mismos datos que las primeras 4
 * mostradas en el dashboard, reutilizando AdminHomeViewModel en vez de duplicar la carga.
 */
@Composable
fun AdminAllAlertsScreen(
    onAlertGoatClick: (String) -> Unit,
    viewModel: AdminHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.admin_home_alerts_title)) }) }
    ) { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (uiState.alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.admin_home_no_alerts_message), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.alerts, key = { it.id }) { alert ->
                        AlertRow(message = alert.message, onClick = alert.goatId?.let { goatId -> { onAlertGoatClick(goatId) } })
                    }
                }
            }
        }
    }
}
