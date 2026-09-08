package com.didiprogrammer.almacaprina.ui.ventas.home

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_cancel
import almacaprina.shared.generated.resources.common_logout_confirm_button
import almacaprina.shared.generated.resources.common_logout_confirm_title
import almacaprina.shared.generated.resources.ventas_home_logout_content_description
import almacaprina.shared.generated.resources.ventas_home_new_sale_cta
import almacaprina.shared.generated.resources.ventas_home_no_active_product
import almacaprina.shared.generated.resources.ventas_home_packaging_out_suffix
import almacaprina.shared.generated.resources.ventas_home_pending_customers_one
import almacaprina.shared.generated.resources.ventas_home_pending_customers_other
import almacaprina.shared.generated.resources.ventas_home_pending_label
import almacaprina.shared.generated.resources.ventas_home_stat_collected_label
import almacaprina.shared.generated.resources.ventas_home_stat_packaging_label
import almacaprina.shared.generated.resources.ventas_home_stat_today_label
import almacaprina.shared.generated.resources.ventas_home_title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.business.formatLongSpanishDate
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.ui.components.RefreshOnResume
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.AmbarBorde
import com.didiprogrammer.almacaprina.ui.theme.AmbarFondo
import com.didiprogrammer.almacaprina.ui.theme.AmbarTexto
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeExtraLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Sección Ventas — Inicio. Ver mockup Ventas-selection.png. */
@Composable
fun VentasHomeScreen(
    onNuevaVentaClick: () -> Unit,
    onPendientesClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: VentasHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold { padding ->
    RefreshableContent(
        isLoading = uiState.isLoading,
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.padding(padding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(formatLongSpanishDate(today), style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        Text(stringResource(Res.string.ventas_home_title), style = MaterialTheme.typography.displaySmall, color = Tinta)
                    }
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(Res.string.ventas_home_logout_content_description),
                            tint = TintaSuave
                        )
                    }
                }
            }

            item {
                val product = uiState.featuredProduct
                val subtitle = if (product != null) {
                    "${product.name} · ${formatCurrency(product.defaultUnitPrice, uiState.currency)} / ${product.saleUnit.label()}"
                } else {
                    stringResource(Res.string.ventas_home_no_active_product)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNuevaVentaClick),
                    shape = ShapeExtraLarge,
                    color = Verde
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(Res.string.ventas_home_new_sale_cta), style = MaterialTheme.typography.headlineSmall, color = SobreVerde)
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SobreVerde)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onPendientesClick),
                    shape = ShapeExtraLarge,
                    color = AmbarFondo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmbarBorde)
                ) {
                    Column(modifier = Modifier.padding(Spacing.xl)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.size(8.dp).background(AmbarTexto, RoundedCornerShape(50))
                            )
                            Text(stringResource(Res.string.ventas_home_pending_label), style = MaterialTheme.typography.labelLarge, color = AmbarTexto)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                formatCurrency(uiState.pendingTotal, uiState.currency),
                                style = MaterialTheme.typography.displaySmall,
                                color = Tinta
                            )
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AmbarTexto)
                        }
                        Text(
                            stringResource(
                                if (uiState.pendingCustomerCount == 1) Res.string.ventas_home_pending_customers_one else Res.string.ventas_home_pending_customers_other,
                                uiState.pendingCustomerCount
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmbarTexto
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeExtraLarge,
                    color = Superficie,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Borde)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
                        HomeStat(
                            label = stringResource(Res.string.ventas_home_stat_today_label),
                            value = "${formatQuantity(uiState.litersSoldToday)} L",
                            modifier = Modifier.weight(1f)
                        )
                        HomeStat(
                            label = stringResource(Res.string.ventas_home_stat_collected_label),
                            value = formatCurrency(uiState.collectedToday, uiState.currency),
                            modifier = Modifier.weight(1f)
                        )
                        HomeStat(
                            label = stringResource(Res.string.ventas_home_stat_packaging_label),
                            value = stringResource(Res.string.ventas_home_packaging_out_suffix, uiState.packagingDepositsOut),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(stringResource(Res.string.common_logout_confirm_title)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout(onLoggedOut)
                }) { Text(stringResource(Res.string.common_logout_confirm_button)) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text(stringResource(Res.string.common_cancel)) } }
        )
    }
}

@Composable
private fun HomeStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TintaSuave)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Tinta)
    }
}
