package com.didiprogrammer.almacaprina.ui.ventas.pendientes

import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import kotlinx.datetime.LocalDate

data class PendingSaleItem(
    val saleId: String,
    val date: LocalDate,
    val description: String,
    val amount: Double
)

/** Ventas · Cobrar pendientes (detalle por cliente). Ver mockup Ventas-selection-details.png. */
data class PendingSalesDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val currency: String = "COP",
    val customerName: String = "",
    val totalPending: Double = 0.0,
    val daysSinceOldest: Int = 0,
    val items: List<PendingSaleItem> = emptyList(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
)
