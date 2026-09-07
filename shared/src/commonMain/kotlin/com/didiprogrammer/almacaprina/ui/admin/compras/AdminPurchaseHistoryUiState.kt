package com.didiprogrammer.almacaprina.ui.admin.compras

import com.didiprogrammer.almacaprina.business.totalCost
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import kotlinx.datetime.LocalDate

data class PurchaseHistoryItem(
    val purchase: Purchase,
    val itemName: String
)

data class AdminPurchaseHistoryUiState(
    val isLoading: Boolean = true,
    val allItems: List<PurchaseHistoryItem> = emptyList(),
    val selectedCategory: PurchaseCategory? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val currency: String = "COP"
) {
    val filteredItems: List<PurchaseHistoryItem>
        get() = allItems
            .filter { selectedCategory == null || it.purchase.category == selectedCategory }
            .filter { fromDate == null || it.purchase.date >= fromDate }
            .filter { toDate == null || it.purchase.date <= toDate }
            .sortedByDescending { it.purchase.date }

    val totalAmount: Double get() = filteredItems.sumOf { it.purchase.totalCost() }
}
