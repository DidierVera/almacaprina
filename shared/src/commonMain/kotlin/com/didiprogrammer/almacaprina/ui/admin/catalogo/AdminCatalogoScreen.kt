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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import com.didiprogrammer.almacaprina.ui.components.AlmacaprinaCard
import com.didiprogrammer.almacaprina.ui.components.StatusChip
import com.didiprogrammer.almacaprina.ui.components.label
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
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo producto")
                }
                CatalogoSubTab.ENVASES -> FloatingActionButton(onClick = { showNewPackagingDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo envase")
                }
                CatalogoSubTab.INSUMOS -> FloatingActionButton(onClick = { showNewInsumoDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo insumo")
                }
                CatalogoSubTab.RECETAS -> if (uiState.selectedRecipeProductId != null) {
                    FloatingActionButton(onClick = { showAddRecipeItemDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar insumo a la receta")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                CatalogoSubTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(tab.label) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
}

@Composable
private fun ProductosList(products: List<Product>, currency: String, onProductClick: (Product) -> Unit) {
    if (products.isEmpty()) {
        EmptyState("Sin productos registrados todavía.")
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
        EmptyState("Sin envases registrados todavía.")
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
                                "Retornable · depósito ${formatCurrency(packaging.depositAmount ?: 0.0, currency)}"
                            } else {
                                "No retornable"
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
        EmptyState("Sin insumos registrados todavía.")
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
                            insumo.lastUnitCost?.let { formatCurrency(it, currency) } ?: "Sin costo",
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
            EmptyState("Primero crea un producto derivado en la pestaña Productos.")
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
            EmptyState("Este producto todavía no tiene insumos en su receta.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recipeItems, key = { it.id }) { item ->
                    val insumo = insumosById[item.insumoId]
                    AlmacaprinaCard {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(insumo?.name ?: "Insumo eliminado", style = MaterialTheme.typography.titleSmall)
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
private fun ActiveChip(active: Boolean) {
    val colors = MaterialTheme.colorScheme
    StatusChip(
        label = if (active) "Activo" else "Inactivo",
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
