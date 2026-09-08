package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.catalogo_sub_tab_envases
import almacaprina.shared.generated.resources.catalogo_sub_tab_insumos
import almacaprina.shared.generated.resources.catalogo_sub_tab_productos
import almacaprina.shared.generated.resources.catalogo_sub_tab_recetas
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import org.jetbrains.compose.resources.stringResource

enum class CatalogoSubTab {
    PRODUCTOS,
    ENVASES,
    INSUMOS,
    RECETAS
}

@Composable
fun CatalogoSubTab.label(): String = stringResource(
    when (this) {
        CatalogoSubTab.PRODUCTOS -> Res.string.catalogo_sub_tab_productos
        CatalogoSubTab.ENVASES -> Res.string.catalogo_sub_tab_envases
        CatalogoSubTab.INSUMOS -> Res.string.catalogo_sub_tab_insumos
        CatalogoSubTab.RECETAS -> Res.string.catalogo_sub_tab_recetas
    }
)

data class AdminCatalogoUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
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
