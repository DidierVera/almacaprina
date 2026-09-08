package com.didiprogrammer.almacaprina.ui.ventas.envases

data class DepositCustomerItem(
    val customerId: String,
    val customerName: String,
    val totalUnitsOut: Int,
    val totalDepositValue: Double
)

/** Ventas · Devolver envase (lista). Agrupa business/packagingUnitsOutByCustomer por cliente. */
data class VentasDepositsListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val currency: String = "COP",
    val totalUnitsOut: Int = 0,
    val totalDepositValue: Double = 0.0,
    val items: List<DepositCustomerItem> = emptyList()
)
