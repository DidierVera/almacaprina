package com.didiprogrammer.almacaprina.ui.ventas.home

import com.didiprogrammer.almacaprina.domain.model.Product

/** Sección Ventas — Inicio. Ver mockup Ventas-selection.png. */
data class VentasHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val currency: String = "COP",
    /** Producto activo destacado en "+ Nueva venta" — hoy "Leche fresca", genérico por category = RAW_MILK. */
    val featuredProduct: Product? = null,
    val pendingTotal: Double = 0.0,
    val pendingCustomerCount: Int = 0,
    val litersSoldToday: Double = 0.0,
    val collectedToday: Double = 0.0,
    val packagingDepositsOut: Int = 0
)
