package com.didiprogrammer.almacaprina.ui.admin.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.business.roundTo1Decimal
import com.didiprogrammer.almacaprina.ui.components.AlertRow
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.QuickActionButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.SectionHeader
import com.didiprogrammer.almacaprina.ui.components.SegmentedRow
import com.didiprogrammer.almacaprina.ui.components.StatCard
import com.didiprogrammer.almacaprina.ui.components.StatTile
import com.didiprogrammer.almacaprina.ui.theme.Riel
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.Verde
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sección 1 — Inicio (vista general) del rol Compras/Admin. Pantalla de solo lectura.
 */
@Composable
fun AdminHomeScreen(
    onHerdStatusClick: (String) -> Unit,
    onSeeAllAlertsClick: () -> Unit,
    onRegistrarCompraClick: () -> Unit,
    onNuevoLoteClick: () -> Unit,
    onNuevaTareaClick: () -> Unit,
    viewModel: AdminHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)

    RefreshableContent(
        isLoading = uiState.isLoading,
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item {
                Text(
                    text = uiState.farmName.ifBlank { "Almacaprina" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Tinta
                )
            }

            uiState.errorMessage?.let { message ->
                item { AlertRow(message = "No se pudo actualizar todo: $message") }
            }

            item { ProductionVsGoalSection(uiState) }

            item { HerdStatusSection(uiState.herdCounts, onHerdStatusClick) }

            item { AlertsSection(uiState.alerts, onSeeAllAlertsClick) }

            item {
                FinancialSummarySection(
                    uiState = uiState,
                    onPeriodSelected = viewModel::onFinancialPeriodSelected
                )
            }

            item {
                QuickActionsSection(
                    onRegistrarCompraClick = onRegistrarCompraClick,
                    onNuevoLoteClick = onNuevoLoteClick,
                    onNuevaTareaClick = onNuevaTareaClick
                )
            }
        }
    }
}

@Composable
private fun ProductionVsGoalSection(uiState: AdminHomeUiState) {
    StatCard(
        title = "Producción vs. meta",
        value = "${formatLiters(uiState.todayLiters)} L"
    ) {
        LinearProgressIndicator(
            progress = { uiState.progressFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Verde,
            trackColor = Riel
        )
        Text(
            text = "Meta: ${formatLiters(uiState.targetLiters)} L/día",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Text(
            text = "Litros disponibles: ${formatLiters(uiState.availableLiters)} L",
            style = MaterialTheme.typography.bodyMedium,
            color = Terracota,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}

private data class HerdStatusItem(val label: String, val count: Int, val filterKey: String)

@Composable
private fun HerdStatusSection(counts: HerdStatusCounts, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(title = "Estado del hato")
        val items = listOf(
            HerdStatusItem("En producción", counts.inProduction, "in_production"),
            HerdStatusItem("Gestantes", counts.pregnant, "pregnant"),
            HerdStatusItem("Secas", counts.dry, "dry"),
            HerdStatusItem("Cabretonas", counts.youngDoes, "young_doe")
        )
        // Grid manual de 2 columnas — items fijos y pocos, no hace falta LazyVerticalGrid
        // (que además no anida bien dentro del LazyColumn de esta pantalla).
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                rowItems.forEach { item ->
                    StatTile(
                        label = item.label,
                        value = item.count.toString(),
                        onClick = { onClick(item.filterKey) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsSection(alerts: List<HomeAlert>, onSeeAllClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(
            title = "Alertas",
            actionLabel = if (alerts.size > 4) "Ver todas" else null,
            onActionClick = if (alerts.size > 4) onSeeAllClick else null
        )
        if (alerts.isEmpty()) {
            AlmacaprinaCard {
                Text("Sin alertas pendientes por ahora.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                alerts.take(4).forEach { alert -> AlertRow(message = alert.message) }
            }
        }
    }
}

@Composable
private fun FinancialSummarySection(
    uiState: AdminHomeUiState,
    onPeriodSelected: (FinancialPeriod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(title = "Resumen financiero")
        SegmentedRow(
            options = FinancialPeriod.entries,
            selected = uiState.financialPeriod,
            onSelect = onPeriodSelected,
            label = { it.label }
        )
        AlmacaprinaCard {
            FinancialRow("Ingresos", formatCurrency(uiState.revenue, uiState.currency))
            FinancialRow("Cartera pendiente", formatCurrency(uiState.pendingReceivable, uiState.currency))
            FinancialRow(
                "Costo por litro",
                uiState.costPerLiter?.let { formatCurrency(it, uiState.currency) } ?: "Sin datos"
            )
        }
    }
}

@Composable
private fun FinancialRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun QuickActionsSection(
    onRegistrarCompraClick: () -> Unit,
    onNuevoLoteClick: () -> Unit,
    onNuevaTareaClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(title = "Accesos rápidos")
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            QuickActionButton("Registrar compra", Icons.Outlined.ShoppingCart, onRegistrarCompraClick)
            QuickActionButton("Nuevo lote", Icons.Outlined.PrecisionManufacturing, onNuevoLoteClick)
            QuickActionButton("Nueva tarea", Icons.Outlined.Event, onNuevaTareaClick)
        }
    }
}

private fun formatLiters(value: Double): String = roundTo1Decimal(value).toString()
