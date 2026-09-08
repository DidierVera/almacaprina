package com.didiprogrammer.almacaprina.ui.admin.produccion

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_production_history_deleted_product_fallback
import almacaprina.shared.generated.resources.admin_production_history_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/** Sección 4, pantalla 4.1 — Historial de lotes de producción. */
class AdminProductionHistoryViewModel(
    private val productionBatchRepository: ProductionBatchRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductionHistoryUiState())
    val uiState: StateFlow<AdminProductionHistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                // Los 2 repositorios son independientes — se piden ambos a la vez.
                val (batches, products) = coroutineScope {
                    val batchesDeferred = async { productionBatchRepository.getAll() }
                    val productsDeferred = async { productRepository.getAll() }
                    batchesDeferred.await() to productsDeferred.await()
                }
                val productsById = products.associateBy { it.id }
                val deletedProductFallback = getString(Res.string.admin_production_history_deleted_product_fallback)
                val items = batches.map { batch ->
                    val product = productsById[batch.outputProductId]
                    BatchHistoryItem(
                        batch = batch,
                        productName = product?.name ?: deletedProductFallback,
                        productUnit = product?.saleUnit
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        allItems = items,
                        derivedProducts = products.filter { p -> p.category == ProductCategory.DERIVED_DAIRY }
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.admin_production_history_error_load)) }
            }
        }
    }

    fun onProductFilterSelected(productId: String?) = _uiState.update { it.copy(selectedProductId = productId) }
}
