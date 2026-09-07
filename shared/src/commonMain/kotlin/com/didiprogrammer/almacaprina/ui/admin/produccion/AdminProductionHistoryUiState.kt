package com.didiprogrammer.almacaprina.ui.admin.produccion

import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductionBatch

data class BatchHistoryItem(
    val batch: ProductionBatch,
    val productName: String,
    val productUnitLabel: String
)

data class AdminProductionHistoryUiState(
    val isLoading: Boolean = true,
    val allItems: List<BatchHistoryItem> = emptyList(),
    val derivedProducts: List<Product> = emptyList(),
    val selectedProductId: String? = null
) {
    val filteredItems: List<BatchHistoryItem>
        get() = allItems
            .filter { selectedProductId == null || it.batch.outputProductId == selectedProductId }
            .sortedByDescending { it.batch.date }
}
