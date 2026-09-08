package com.didiprogrammer.almacaprina.ui.admin.catalogo

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_catalog_error_load
import almacaprina.shared.generated.resources.common_error_save_failed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.InsumoCategory
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import com.didiprogrammer.almacaprina.domain.model.SaleUnit
import com.didiprogrammer.almacaprina.domain.model.UnitOfMeasure
import com.didiprogrammer.almacaprina.domain.repository.BreedRepository
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductPackagingOptionRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRecipeItemRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/** Bundle de los datos crudos del catálogo — se piden todos en paralelo, ver [AdminCatalogoViewModel.load]. */
private data class AdminCatalogoRawData(
    val products: List<Product>,
    val packagings: List<Packaging>,
    val insumos: List<Insumo>,
    val recipeItems: List<ProductRecipeItem>,
    val productPackagingOptions: List<ProductPackagingOption>,
    val breeds: List<Breed>,
    val currency: String
)

/**
 * Sección 3 — Catálogo. Un solo ViewModel para los 6 sub-tabs (Productos, Envases,
 * Insumos, Recetas, Empaques, Razas) porque son catálogos pequeños que se cargan completos de una vez.
 */
class AdminCatalogoViewModel(
    private val productRepository: ProductRepository,
    private val packagingRepository: PackagingRepository,
    private val insumoRepository: InsumoRepository,
    private val productRecipeItemRepository: ProductRecipeItemRepository,
    private val productPackagingOptionRepository: ProductPackagingOptionRepository,
    private val breedRepository: BreedRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCatalogoUiState())
    val uiState: StateFlow<AdminCatalogoUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                // Los 7 repositorios son independientes — se piden todos a la vez.
                val raw = coroutineScope {
                    val productsDeferred = async { productRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    val recipeItemsDeferred = async { productRecipeItemRepository.getAll() }
                    val productPackagingOptionsDeferred = async { productPackagingOptionRepository.getAll() }
                    val breedsDeferred = async { breedRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    AdminCatalogoRawData(
                        products = productsDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        insumos = insumosDeferred.await(),
                        recipeItems = recipeItemsDeferred.await(),
                        productPackagingOptions = productPackagingOptionsDeferred.await(),
                        breeds = breedsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                _uiState.update {
                    val defaultRecipeProduct = it.selectedRecipeProductId
                        ?: raw.products.firstOrNull { p -> p.category == ProductCategory.DERIVED_DAIRY }?.id
                    val defaultPackagingProduct = it.selectedPackagingProductId ?: raw.products.firstOrNull()?.id
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        currency = raw.currency,
                        products = raw.products,
                        packagings = raw.packagings,
                        insumos = raw.insumos,
                        recipeItems = raw.recipeItems,
                        selectedRecipeProductId = defaultRecipeProduct,
                        productPackagingOptions = raw.productPackagingOptions,
                        selectedPackagingProductId = defaultPackagingProduct,
                        breeds = raw.breeds
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.admin_catalog_error_load)) }
            }
        }
    }

    fun onTabSelected(tab: CatalogoSubTab) = _uiState.update { it.copy(selectedTab = tab) }
    fun onRecipeProductSelected(productId: String) = _uiState.update { it.copy(selectedRecipeProductId = productId) }
    fun onPackagingProductSelected(productId: String) = _uiState.update { it.copy(selectedPackagingProductId = productId) }

    fun addProduct(name: String, category: ProductCategory, saleUnit: SaleUnit, defaultUnitPrice: Double, active: Boolean) {
        save {
            productRepository.insert(
                Product(id = newId(), name = name, category = category, saleUnit = saleUnit, defaultUnitPrice = defaultUnitPrice, active = active)
            )
        }
    }

    fun updateProduct(existing: Product, name: String, category: ProductCategory, saleUnit: SaleUnit, defaultUnitPrice: Double, active: Boolean) {
        save {
            productRepository.update(
                existing.id,
                existing.copy(name = name, category = category, saleUnit = saleUnit, defaultUnitPrice = defaultUnitPrice, active = active)
            )
        }
    }

    fun addPackaging(name: String, isReturnable: Boolean, depositAmount: Double?, unitCost: Double) {
        save {
            packagingRepository.insert(
                Packaging(id = newId(), name = name, isReturnable = isReturnable, depositAmount = depositAmount, unitCost = unitCost)
            )
        }
    }

    fun updatePackaging(existing: Packaging, name: String, isReturnable: Boolean, depositAmount: Double?, unitCost: Double) {
        save {
            packagingRepository.update(
                existing.id,
                existing.copy(name = name, isReturnable = isReturnable, depositAmount = depositAmount, unitCost = unitCost)
            )
        }
    }

    fun addInsumo(
        name: String,
        category: InsumoCategory,
        unitOfMeasure: UnitOfMeasure,
        active: Boolean,
        purchasePackageLabel: String?,
        purchasePackageSize: Double?,
        notes: String?
    ) {
        save {
            insumoRepository.insert(
                Insumo(
                    id = newId(),
                    name = name,
                    category = category,
                    unitOfMeasure = unitOfMeasure,
                    active = active,
                    purchasePackageLabel = purchasePackageLabel,
                    purchasePackageSize = purchasePackageSize,
                    notes = notes
                )
            )
        }
    }

    // Preserva last_unit_cost y reorder_lead_time_days (no editables desde este formulario,
    // los actualiza el flujo de Compras y la configuración de alertas respectivamente).
    fun updateInsumo(
        existing: Insumo,
        name: String,
        category: InsumoCategory,
        unitOfMeasure: UnitOfMeasure,
        active: Boolean,
        purchasePackageLabel: String?,
        purchasePackageSize: Double?,
        notes: String?
    ) {
        save {
            insumoRepository.update(
                existing.id,
                existing.copy(
                    name = name,
                    category = category,
                    unitOfMeasure = unitOfMeasure,
                    active = active,
                    purchasePackageLabel = purchasePackageLabel,
                    purchasePackageSize = purchasePackageSize,
                    notes = notes
                )
            )
        }
    }

    fun addRecipeItem(productId: String, insumoId: String, quantityPerOutputUnit: Double) {
        save {
            productRecipeItemRepository.insert(
                ProductRecipeItem(id = newId(), productId = productId, insumoId = insumoId, quantityPerOutputUnit = quantityPerOutputUnit)
            )
        }
    }

    /** Ver CLAUDE.md — qué envases/empaques aplican a cada producto, para precargarlos en
     * "Nueva venta" (Ventas). Solo puede haber un default por producto. */
    fun addProductPackagingOption(productId: String, packagingId: String, isDefault: Boolean) {
        save {
            if (isDefault) clearDefaultPackagingOption(productId)
            productPackagingOptionRepository.insert(
                ProductPackagingOption(id = newId(), productId = productId, packagingId = packagingId, isDefault = isDefault)
            )
        }
    }

    fun setDefaultPackagingOption(option: ProductPackagingOption) {
        save {
            clearDefaultPackagingOption(option.productId, exceptId = option.id)
            productPackagingOptionRepository.update(option.id, option.copy(isDefault = true))
        }
    }

    fun deleteProductPackagingOption(id: String) {
        save {
            productPackagingOptionRepository.delete(id)
        }
    }

    fun addBreed(name: String, prefix: String?) {
        save {
            breedRepository.insert(Breed(id = newId(), name = name, prefix = prefix))
        }
    }

    fun updateBreed(existing: Breed, name: String, prefix: String?) {
        save {
            breedRepository.update(existing.id, existing.copy(name = name, prefix = prefix))
        }
    }

    private suspend fun clearDefaultPackagingOption(productId: String, exceptId: String? = null) {
        _uiState.value.productPackagingOptions
            .filter { it.productId == productId && it.isDefault && it.id != exceptId }
            .forEach { productPackagingOptionRepository.update(it.id, it.copy(isDefault = false)) }
    }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                block()
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.common_error_save_failed)) }
            }
        }
    }
}
