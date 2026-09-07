package com.didiprogrammer.almacaprina.ui.ventas.pendientes

import kotlinx.datetime.LocalDate

data class PendingCustomerItem(
    val customerId: String,
    val customerName: String,
    val totalPending: Double,
    val pendingSalesCount: Int,
    val daysSinceOldest: Int
)

/** Ventas · Cobrar pendientes (lista). Ver mockup Ventas-selection-pending.png. */
data class PendingSalesListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val currency: String = "COP",
    val totalPending: Double = 0.0,
    val items: List<PendingCustomerItem> = emptyList(),
    val today: LocalDate? = null
)
