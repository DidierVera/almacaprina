package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.new_sale_cliente_create_button
import almacaprina.shared.generated.resources.new_sale_cliente_last_sale_days
import almacaprina.shared.generated.resources.new_sale_cliente_new_contact_placeholder
import almacaprina.shared.generated.resources.new_sale_cliente_new_customer_title
import almacaprina.shared.generated.resources.new_sale_cliente_new_name_placeholder
import almacaprina.shared.generated.resources.new_sale_cliente_search_placeholder
import almacaprina.shared.generated.resources.new_sale_cliente_title
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.PrimaryButton
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.ui.theme.BordeControl
import com.didiprogrammer.almacaprina.ui.theme.Spacing
import com.didiprogrammer.almacaprina.ui.theme.Terracota
import com.didiprogrammer.almacaprina.ui.theme.Tinta
import com.didiprogrammer.almacaprina.ui.theme.TintaSuave
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.stringResource

/** Nueva venta · Paso 1 — ¿Para quién? Ver mockup Ventas-selection-clientes.png. */
@Composable
fun NewSaleClienteScreen(
    viewModel: NewSaleViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
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
                item { NewSaleStepHeader(step = 1, title = stringResource(Res.string.new_sale_cliente_title), onBack = onBack) }

                item {
                    OutlinedTextField(
                        value = uiState.customerSearchQuery,
                        onValueChange = viewModel::onCustomerSearchChanged,
                        placeholder = { Text(stringResource(Res.string.new_sale_cliente_search_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                items(uiState.filteredCustomers, key = { it.id }) { customer ->
                    val lastSaleDate = uiState.lastSaleDateByCustomer[customer.id]
                    val subtitle = if (lastSaleDate != null && uiState.today != null) {
                        stringResource(Res.string.new_sale_cliente_last_sale_days, customer.type.label(), lastSaleDate.daysUntil(uiState.today!!))
                    } else {
                        customer.type.label()
                    }
                    AlmacaprinaCard(onClick = {
                        viewModel.onCustomerSelected(customer)
                        onContinue()
                    }) {
                        Text(customer.name, style = MaterialTheme.typography.titleSmall, color = Tinta)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TintaSuave)
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        border = BorderStroke(1.dp, BordeControl)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            Text(stringResource(Res.string.new_sale_cliente_new_customer_title), style = MaterialTheme.typography.titleSmall, color = Tinta)
                            OutlinedTextField(
                                value = uiState.newCustomerName,
                                onValueChange = viewModel::onNewCustomerNameChanged,
                                placeholder = { Text(stringResource(Res.string.new_sale_cliente_new_name_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.newCustomerContact,
                                onValueChange = viewModel::onNewCustomerContactChanged,
                                placeholder = { Text(stringResource(Res.string.new_sale_cliente_new_contact_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            PrimaryButton(
                                text = stringResource(Res.string.new_sale_cliente_create_button),
                                enabled = uiState.canCreateNewCustomer && !uiState.isSaving,
                                loading = uiState.isSaving,
                                onClick = { viewModel.createCustomerAndContinue(onContinue) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
