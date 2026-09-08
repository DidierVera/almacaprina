package com.didiprogrammer.almacaprina.ui.ventas.envases

data class DepositPackagingRow(
    val packagingId: String,
    val packagingName: String,
    val unitsOut: Int,
    val depositAmountPerUnit: Double,
    val returnQuantityText: String
) {
    val returnQuantity: Int? get() = returnQuantityText.toIntOrNull()?.takeIf { it in 1..unitsOut }
    val refundAmount: Double get() = (returnQuantity ?: 0) * depositAmountPerUnit
}

/** Ventas · Devolver envase (detalle por cliente) — un renglón por tipo de envase que tiene fuera. */
data class VentasDepositsDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currency: String = "COP",
    val customerName: String = "",
    val rows: List<DepositPackagingRow> = emptyList()
)
