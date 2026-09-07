package com.didiprogrammer.almacaprina.ui.admin.catalogo

import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem

enum class CatalogoSubTab(val label: String) {
    PRODUCTOS("Productos"),
    ENVASES("Envases"),
    INSUMOS("Insumos"),
    RECETAS("Recetas")
}

data class AdminCatalogoUiState(
    val isLoading: Boolean = true,
    val currency: String = "COP",
    val selectedTab: CatalogoSubTab = CatalogoSubTab.PRODUCTOS,
    val products: List<Product> = emptyList(),
    val packagings: List<Packaging> = emptyList(),
    val insumos: List<Insumo> = emptyList(),
    val recipeItems: List<ProductRecipeItem> = emptyList(),
    val selectedRecipeProductId: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val derivedProducts: List<Product> get() = products.filter { it.category == ProductCategory.DERIVED_DAIRY }

    val selectedRecipeItems: List<ProductRecipeItem>
        get() = recipeItems.filter { it.productId == selectedRecipeProductId }
}
