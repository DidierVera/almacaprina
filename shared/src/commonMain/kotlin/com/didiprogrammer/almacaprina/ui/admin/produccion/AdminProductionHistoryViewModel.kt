package com.didiprogrammer.almacaprina.ui.admin.produccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository
import com.didiprogrammer.almacaprina.ui.components.label
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Los 2 repositorios son independientes — se piden ambos a la vez.
            val (batches, products) = coroutineScope {
                val batchesDeferred = async { productionBatchRepository.getAll() }
                val productsDeferred = async { productRepository.getAll() }
                batchesDeferred.await() to productsDeferred.await()
            }
            val productsById = products.associateBy { it.id }
            val items = batches.map { batch ->
                val product = productsById[batch.outputProductId]
                BatchHistoryItem(
                    batch = batch,
                    productName = product?.name ?: "Producto eliminado",
                    productUnitLabel = product?.saleUnit?.label() ?: ""
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allItems = items,
                    derivedProducts = products.filter { p -> p.category == ProductCategory.DERIVED_DAIRY }
                )
            }
        }
    }

    fun onProductFilterSelected(productId: String?) = _uiState.update { it.copy(selectedProductId = productId) }
}
