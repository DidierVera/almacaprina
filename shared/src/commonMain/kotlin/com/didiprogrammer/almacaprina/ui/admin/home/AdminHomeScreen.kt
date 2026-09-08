package com.didiprogrammer.almacaprina.ui.admin.home

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_home_alerts_title
import almacaprina.shared.generated.resources.admin_home_available_liters_label
import almacaprina.shared.generated.resources.admin_home_cost_per_liter_label
import almacaprina.shared.generated.resources.admin_home_financial_summary_title
import almacaprina.shared.generated.resources.admin_home_goal_label
import almacaprina.shared.generated.resources.admin_home_herd_dry_plural
import almacaprina.shared.generated.resources.admin_home_herd_pregnant_plural
import almacaprina.shared.generated.resources.admin_home_herd_status_title
import almacaprina.shared.generated.resources.admin_home_herd_young_does_plural
import almacaprina.shared.generated.resources.admin_home_new_batch_action
import almacaprina.shared.generated.resources.admin_home_new_task_action
import almacaprina.shared.generated.resources.admin_home_no_alerts_message
import almacaprina.shared.generated.resources.admin_home_no_data_fallback
import almacaprina.shared.generated.resources.admin_home_partial_error_prefix
import almacaprina.shared.generated.resources.admin_home_pending_receivable_label
import almacaprina.shared.generated.resources.admin_home_production_vs_goal_title
import almacaprina.shared.generated.resources.admin_home_quick_actions_title
import almacaprina.shared.generated.resources.admin_home_register_purchase_action
import almacaprina.shared.generated.resources.admin_home_revenue_label
import almacaprina.shared.generated.resources.admin_home_see_all_action
import almacaprina.shared.generated.resources.goat_status_in_production
import almacaprina.shared.generated.resources.login_app_name
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
import org.jetbrains.compose.resources.stringResource
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
                    text = uiState.farmName.ifBlank { stringResource(Res.string.login_app_name) },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Tinta
                )
            }

            uiState.errorMessage?.let { message ->
                item { AlertRow(message = stringResource(Res.string.admin_home_partial_error_prefix, message)) }
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
        title = stringResource(Res.string.admin_home_production_vs_goal_title),
        value = "${formatLiters(uiState.todayLiters)} L"
    ) {
        LinearProgressIndicator(
            progress = { uiState.progressFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Verde,
            trackColor = Riel
        )
        Text(
            text = stringResource(Res.string.admin_home_goal_label, formatLiters(uiState.targetLiters)),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Text(
            text = stringResource(Res.string.admin_home_available_liters_label, formatLiters(uiState.availableLiters)),
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
        SectionHeader(title = stringResource(Res.string.admin_home_herd_status_title))
        val items = listOf(
            HerdStatusItem(stringResource(Res.string.goat_status_in_production), counts.inProduction, "in_production"),
            HerdStatusItem(stringResource(Res.string.admin_home_herd_pregnant_plural), counts.pregnant, "pregnant"),
            HerdStatusItem(stringResource(Res.string.admin_home_herd_dry_plural), counts.dry, "dry"),
            HerdStatusItem(stringResource(Res.string.admin_home_herd_young_does_plural), counts.youngDoes, "young_doe")
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
            title = stringResource(Res.string.admin_home_alerts_title),
            actionLabel = if (alerts.size > 4) stringResource(Res.string.admin_home_see_all_action) else null,
            onActionClick = if (alerts.size > 4) onSeeAllClick else null
        )
        if (alerts.isEmpty()) {
            AlmacaprinaCard {
                Text(stringResource(Res.string.admin_home_no_alerts_message), style = MaterialTheme.typography.bodyMedium)
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
        SectionHeader(title = stringResource(Res.string.admin_home_financial_summary_title))
        SegmentedRow(
            options = FinancialPeriod.entries,
            selected = uiState.financialPeriod,
            onSelect = onPeriodSelected,
            label = { it.label() }
        )
        AlmacaprinaCard {
            FinancialRow(stringResource(Res.string.admin_home_revenue_label), formatCurrency(uiState.revenue, uiState.currency))
            FinancialRow(stringResource(Res.string.admin_home_pending_receivable_label), formatCurrency(uiState.pendingReceivable, uiState.currency))
            FinancialRow(
                stringResource(Res.string.admin_home_cost_per_liter_label),
                uiState.costPerLiter?.let { formatCurrency(it, uiState.currency) } ?: stringResource(Res.string.admin_home_no_data_fallback)
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
        SectionHeader(title = stringResource(Res.string.admin_home_quick_actions_title))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            QuickActionButton(stringResource(Res.string.admin_home_register_purchase_action), Icons.Outlined.ShoppingCart, onRegistrarCompraClick)
            QuickActionButton(stringResource(Res.string.admin_home_new_batch_action), Icons.Outlined.PrecisionManufacturing, onNuevoLoteClick)
            QuickActionButton(stringResource(Res.string.admin_home_new_task_action), Icons.Outlined.Event, onNuevaTareaClick)
        }
    }
}

private fun formatLiters(value: Double): String = roundTo1Decimal(value).toString()
