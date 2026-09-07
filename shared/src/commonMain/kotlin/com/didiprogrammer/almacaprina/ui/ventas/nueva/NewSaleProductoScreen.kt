package com.didiprogrammer.almacaprina.ui.ventas.nueva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.Borde
import com.didiprogrammer.almacaprina.ui.theme.ShapeLarge
import com.didiprogrammer.almacaprina.ui.theme.SobreVerde
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Superficie
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import com.didiprogrammer.almacaprina.ui.theme.Verde

/** Nueva venta · Paso 2 — ¿Qué vas a vender? Ver mockup Ventas-selection-product.png. */
@Composable
fun NewSaleProductoScreen(
    viewModel: NewSaleViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        RefreshableContent(
            isLoading = uiState.isLoading,
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    NewSaleStepHeader(
                        step = 2,
                        title = "¿Qué vas a vender?",
                        onBack = onBack,
                        subtitle = uiState.selectedCustomer?.name
                    )
                }

                items(uiState.activeProducts, key = { it.id }) { product ->
                    val selected = uiState.selectedProduct?.id == product.id
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.onProductSelected(product) },
                        shape = ShapeLarge,
                        color = if (selected) Verde.copy(alpha = 0.12f) else Superficie,
                        border = BorderStroke(1.dp, if (selected) Verde else Borde)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Surface(shape = CircleShape, color = if (selected) Verde else Borde, modifier = Modifier.size(28.dp)) {
                                if (selected) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = SobreVerde, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleSmall, color = Tinta)
                                Text(
                                    "Venta por ${product.saleUnit.label().lowercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TintaSuave
                                )
                            }
                            Text(
                                formatCurrency(product.defaultUnitPrice, uiState.currency),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) Verde else Tinta
                            )
                        }
                    }
                }

                item {
                    Text(
                        "El catálogo lo administra Compras/Admin. Los productos que se activen allí " +
                            "aparecen aquí sin cambiar esta pantalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TintaSuave
                    )
                }

                item {
                    PrimaryButton(
                        text = "Continuar",
                        enabled = uiState.canContinueFromProducto,
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
