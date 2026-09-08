package com.didiprogrammer.almacaprina.ui.admin.catalogo

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.didiprogrammer.almacaprina.business.formatCurrency
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_catalog_active_label
import almacaprina.shared.generated.resources.admin_catalog_add_recipe_item_content_description
import almacaprina.shared.generated.resources.admin_catalog_add_product_packaging_content_description
import almacaprina.shared.generated.resources.admin_catalog_empty_recipe_message
import almacaprina.shared.generated.resources.admin_catalog_empty_product_packaging_message
import almacaprina.shared.generated.resources.admin_catalog_inactive_label
import almacaprina.shared.generated.resources.admin_catalog_insumos_empty
import almacaprina.shared.generated.resources.admin_catalog_new_insumo_content_description
import almacaprina.shared.generated.resources.admin_catalog_new_packaging_content_description
import almacaprina.shared.generated.resources.admin_catalog_new_product_content_description
import almacaprina.shared.generated.resources.admin_catalog_no_cost_fallback
import almacaprina.shared.generated.resources.admin_catalog_no_derived_products_message
import almacaprina.shared.generated.resources.admin_catalog_no_products_message
import almacaprina.shared.generated.resources.admin_catalog_packaging_default_label
import almacaprina.shared.generated.resources.admin_catalog_packaging_not_returnable_label
import almacaprina.shared.generated.resources.admin_catalog_packaging_returnable_label
import almacaprina.shared.generated.resources.admin_catalog_packagings_empty
import almacaprina.shared.generated.resources.admin_catalog_products_empty
import almacaprina.shared.generated.resources.admin_catalog_remove_product_packaging_content_description
import almacaprina.shared.generated.resources.admin_catalog_set_default_content_description
import almacaprina.shared.generated.resources.admin_new_batch_deleted_insumo_fallback
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.RefreshableContent
import com.didiprogrammer.almacaprina.ui.components.StatusChip
import com.didiprogrammer.almacaprina.ui.components.label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Sección 3 — Catálogo (Productos / Envases / Insumos / Recetas). */
@Composable
fun AdminCatalogoScreen(viewModel: AdminCatalogoViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.didiprogrammer.almacaprina.ui.components.RefreshOnResume(viewModel::load)
    var showNewProductDialog by remember { mutableStateOf(false) }
    var showNewPackagingDialog by remember { mutableStateOf(false) }
    var showNewInsumoDialog by remember { mutableStateOf(false) }
    var showAddRecipeItemDialog by remember { mutableStateOf(false) }
    var showAddProductPackagingDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editingPackaging by remember { mutableStateOf<Packaging?>(null) }
    var editingInsumo by remember { mutableStateOf<Insumo?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            when (uiState.selectedTab) {
                CatalogoSubTab.PRODUCTOS -> FloatingActionButton(onClick = { showNewProductDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_catalog_new_product_content_description))
                }
                CatalogoSubTab.ENVASES -> FloatingActionButton(onClick = { showNewPackagingDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_catalog_new_packaging_content_description))
                }
                CatalogoSubTab.INSUMOS -> FloatingActionButton(onClick = { showNewInsumoDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_catalog_new_insumo_content_description))
                }
                CatalogoSubTab.RECETAS -> if (uiState.selectedRecipeProductId != null) {
                    FloatingActionButton(onClick = { showAddRecipeItemDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_catalog_add_recipe_item_content_description))
                    }
                }
                CatalogoSubTab.EMPAQUES -> if (uiState.selectedPackagingProductId != null) {
                    FloatingActionButton(onClick = { showAddProductPackagingDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.admin_catalog_add_product_packaging_content_description))
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(CatalogoSubTab.entries) { tab ->
                    FilterChip(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        label = { Text(tab.label()) }
                    )
                }
            }

            RefreshableContent(
                isLoading = uiState.isLoading,
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                when (uiState.selectedTab) {
                    CatalogoSubTab.PRODUCTOS -> ProductosList(uiState.products, uiState.currency, onProductClick = { editingProduct = it })
                    CatalogoSubTab.ENVASES -> EnvasesList(uiState.packagings, uiState.currency, onPackagingClick = { editingPackaging = it })
                    CatalogoSubTab.INSUMOS -> InsumosList(uiState.insumos, uiState.currency, onInsumoClick = { editingInsumo = it })
                    CatalogoSubTab.RECETAS -> RecetasContent(
                        derivedProducts = uiState.derivedProducts,
                        selectedProductId = uiState.selectedRecipeProductId,
                        recipeItems = uiState.selectedRecipeItems,
                        insumosById = uiState.insumos.associateBy { it.id },
                        onProductSelected = viewModel::onRecipeProductSelected
                    )
                    CatalogoSubTab.EMPAQUES -> EmpaquesContent(
                        products = uiState.products,
                        selectedProductId = uiState.selectedPackagingProductId,
                        options = uiState.selectedProductPackagingOptions,
                        packagingsById = uiState.packagings.associateBy { it.id },
                        onProductSelected = viewModel::onPackagingProductSelected,
                        onSetDefault = viewModel::setDefaultPackagingOption,
                        onRemove = viewModel::deleteProductPackagingOption
                    )
                }
            }
        }
    }

    if (showNewProductDialog) {
        ProductFormDialog(
            existing = null,
            onDismiss = { showNewProductDialog = false },
            onSave = { name, category, saleUnit, price, active ->
                viewModel.addProduct(name, category, saleUnit, price, active)
                showNewProductDialog = false
            }
        )
    }
    editingProduct?.let { product ->
        ProductFormDialog(
            existing = product,
            onDismiss = { editingProduct = null },
            onSave = { name, category, saleUnit, price, active ->
                viewModel.updateProduct(product, name, category, saleUnit, price, active)
                editingProduct = null
            }
        )
    }
    if (showNewPackagingDialog) {
        PackagingFormDialog(
            existing = null,
            onDismiss = { showNewPackagingDialog = false },
            onSave = { name, isReturnable, deposit, unitCost ->
                viewModel.addPackaging(name, isReturnable, deposit, unitCost)
                showNewPackagingDialog = false
            }
        )
    }
    editingPackaging?.let { packaging ->
        PackagingFormDialog(
            existing = packaging,
            onDismiss = { editingPackaging = null },
            onSave = { name, isReturnable, deposit, unitCost ->
                viewModel.updatePackaging(packaging, name, isReturnable, deposit, unitCost)
                editingPackaging = null
            }
        )
    }
    if (showNewInsumoDialog) {
        InsumoFormDialog(
            existing = null,
            onDismiss = { showNewInsumoDialog = false },
            onSave = { name, category, unitOfMeasure, active, purchasePackageLabel, purchasePackageSize, notes ->
                viewModel.addInsumo(name, category, unitOfMeasure, active, purchasePackageLabel, purchasePackageSize, notes)
                showNewInsumoDialog = false
            }
        )
    }
    editingInsumo?.let { insumo ->
        InsumoFormDialog(
            existing = insumo,
            onDismiss = { editingInsumo = null },
            onSave = { name, category, unitOfMeasure, active, purchasePackageLabel, purchasePackageSize, notes ->
                viewModel.updateInsumo(insumo, name, category, unitOfMeasure, active, purchasePackageLabel, purchasePackageSize, notes)
                editingInsumo = null
            }
        )
    }
    if (showAddRecipeItemDialog && uiState.selectedRecipeProductId != null) {
        AddRecipeItemDialog(
            insumos = uiState.insumos,
            onDismiss = { showAddRecipeItemDialog = false },
            onSave = { insumoId, quantity ->
                viewModel.addRecipeItem(uiState.selectedRecipeProductId!!, insumoId, quantity)
                showAddRecipeItemDialog = false
            }
        )
    }
    if (showAddProductPackagingDialog && uiState.selectedPackagingProductId != null) {
        val alreadyLinkedIds = uiState.selectedProductPackagingOptions.map { it.packagingId }.toSet()
        AddProductPackagingDialog(
            availablePackagings = uiState.packagings.filterNot { it.id in alreadyLinkedIds },
            onDismiss = { showAddProductPackagingDialog = false },
            onSave = { packagingId, isDefault ->
                viewModel.addProductPackagingOption(uiState.selectedPackagingProductId!!, packagingId, isDefault)
                showAddProductPackagingDialog = false
            }
        )
    }
}

@Composable
private fun ProductosList(products: List<Product>, currency: String, onProductClick: (Product) -> Unit) {
    if (products.isEmpty()) {
        EmptyState(stringResource(Res.string.admin_catalog_products_empty))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(products, key = { it.id }) { product ->
            AlmacaprinaCard(onClick = { onProductClick(product) }) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(product.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${product.category.label()} · ${product.saleUnit.label()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatCurrency(product.defaultUnitPrice, currency), style = MaterialTheme.typography.titleSmall)
                        ActiveChip(product.active)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnvasesList(packagings: List<Packaging>, currency: String, onPackagingClick: (Packaging) -> Unit) {
    if (packagings.isEmpty()) {
        EmptyState(stringResource(Res.string.admin_catalog_packagings_empty))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(packagings, key = { it.id }) { packaging ->
            AlmacaprinaCard(onClick = { onPackagingClick(packaging) }) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(packaging.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (packaging.isReturnable) {
                                stringResource(Res.string.admin_catalog_packaging_returnable_label, formatCurrency(packaging.depositAmount ?: 0.0, currency))
                            } else {
                                stringResource(Res.string.admin_catalog_packaging_not_returnable_label)
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(formatCurrency(packaging.unitCost, currency), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun InsumosList(insumos: List<Insumo>, currency: String, onInsumoClick: (Insumo) -> Unit) {
    if (insumos.isEmpty()) {
        EmptyState(stringResource(Res.string.admin_catalog_insumos_empty))
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(insumos, key = { it.id }) { insumo ->
            AlmacaprinaCard(onClick = { onInsumoClick(insumo) }) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(insumo.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${insumo.category.label()} · ${insumo.unitOfMeasure.label()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            insumo.lastUnitCost?.let { formatCurrency(it, currency) } ?: stringResource(Res.string.admin_catalog_no_cost_fallback),
                            style = MaterialTheme.typography.titleSmall
                        )
                        ActiveChip(insumo.active)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecetasContent(
    derivedProducts: List<Product>,
    selectedProductId: String?,
    recipeItems: List<ProductRecipeItem>,
    insumosById: Map<String, Insumo>,
    onProductSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (derivedProducts.isEmpty()) {
            EmptyState(stringResource(Res.string.admin_catalog_no_derived_products_message))
            return
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(derivedProducts, key = { it.id }) { product ->
                FilterChip(
                    selected = selectedProductId == product.id,
                    onClick = { onProductSelected(product.id) },
                    label = { Text(product.name) }
                )
            }
        }
        if (recipeItems.isEmpty()) {
            EmptyState(stringResource(Res.string.admin_catalog_empty_recipe_message))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recipeItems, key = { it.id }) { item ->
                    val insumo = insumosById[item.insumoId]
                    AlmacaprinaCard {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(insumo?.name ?: stringResource(Res.string.admin_new_batch_deleted_insumo_fallback), style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${item.quantityPerOutputUnit} ${insumo?.unitOfMeasure?.label() ?: ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmpaquesContent(
    products: List<Product>,
    selectedProductId: String?,
    options: List<ProductPackagingOption>,
    packagingsById: Map<String, Packaging>,
    onProductSelected: (String) -> Unit,
    onSetDefault: (ProductPackagingOption) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (products.isEmpty()) {
            EmptyState(stringResource(Res.string.admin_catalog_no_products_message))
            return
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                FilterChip(
                    selected = selectedProductId == product.id,
                    onClick = { onProductSelected(product.id) },
                    label = { Text(product.name) }
                )
            }
        }
        if (options.isEmpty()) {
            EmptyState(stringResource(Res.string.admin_catalog_empty_product_packaging_message))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(options, key = { it.id }) { option ->
                    val packaging = packagingsById[option.packagingId]
                    AlmacaprinaCard {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(packaging?.name ?: stringResource(Res.string.admin_new_batch_deleted_insumo_fallback), style = MaterialTheme.typography.titleSmall)
                                if (option.isDefault) {
                                    Text(stringResource(Res.string.admin_catalog_packaging_default_label), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onSetDefault(option) }) {
                                    Icon(
                                        if (option.isDefault) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = stringResource(Res.string.admin_catalog_set_default_content_description)
                                    )
                                }
                                IconButton(onClick = { onRemove(option.id) }) {
                                    Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.admin_catalog_remove_product_packaging_content_description))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveChip(active: Boolean) {
    val colors = MaterialTheme.colorScheme
    StatusChip(
        label = stringResource(if (active) Res.string.admin_catalog_active_label else Res.string.admin_catalog_inactive_label),
        containerColor = if (active) colors.primaryContainer else colors.surfaceVariant,
        contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
