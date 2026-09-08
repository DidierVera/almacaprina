package com.didiprogrammer.almacaprina.ui.ventas.nueva

import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption
import kotlinx.datetime.LocalDate

/** Wizard de 4 pasos "Nueva venta" — un solo estado compartido por las 4 pantallas. */
data class NewSaleUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val currency: String = "COP",
    val today: LocalDate? = null,

    // Paso 1 — ¿Para quién?
    val customers: List<Customer> = emptyList(),
    val lastSaleDateByCustomer: Map<String, LocalDate> = emptyMap(),
    val customerSearchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val showNewCustomerForm: Boolean = false,
    val newCustomerName: String = "",
    val newCustomerContact: String = "",

    // Paso 2 — ¿Qué vas a vender?
    val activeProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,

    // Paso 3 — Cantidad y envase
    val quantityText: String = "",
    val packagings: List<Packaging> = emptyList(),
    /** Ver CLAUDE.md — qué envases aplican a cada producto (Catálogo > Empaques, Admin). */
    val productPackagingOptions: List<ProductPackagingOption> = emptyList(),
    val selectedPackagingId: String? = null,
    /**
     * Null = por defecto, 1 envase nuevo por unidad vendida (ver CLAUDE.md). El usuario puede
     * bajarlo (incluso a 0) cuando el cliente reutiliza un envase que ya tenía — confirmado con
     * el dueño: en ese caso no se cobra depósito adicional.
     */
    val newPackagingUnitsOverride: Int? = null,

    // Paso 4 — Confirmar
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID
) {
    val filteredCustomers: List<Customer>
        get() = if (customerSearchQuery.isBlank()) {
            customers
        } else {
            customers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }
        }

    val quantity: Double? get() = quantityText.toDoubleOrNull()
    val selectedPackaging: Packaging? get() = packagings.firstOrNull { it.id == selectedPackagingId }

    /** Envases asociados al producto elegido en el Paso 2 (Catálogo > Empaques, Admin). Si el
     * producto todavía no tiene ninguno configurado, se muestran todos — evita romper el flujo
     * mientras el catálogo de empaques se termina de cargar por producto. */
    val availablePackagings: List<Packaging>
        get() {
            val product = selectedProduct ?: return packagings
            val optionIds = productPackagingOptions.filter { it.productId == product.id }.map { it.packagingId }.toSet()
            return if (optionIds.isEmpty()) packagings else packagings.filter { it.id in optionIds }
        }

    /** Ver CLAUDE.md: las botellas son siempre de 1 L para Leche fresca — 1 envase por unidad vendida,
     * salvo que el usuario lo haya ajustado manualmente (ver [newPackagingUnitsOverride]). */
    val newPackagingUnitsCount: Int get() = newPackagingUnitsOverride ?: (quantity?.toInt() ?: 0)

    val depositCharged: Double
        get() = selectedPackaging?.takeIf { it.isReturnable }?.let { pkg ->
            newPackagingUnitsCount * (pkg.depositAmount ?: 0.0)
        } ?: 0.0

    val subtotal: Double get() = (quantity ?: 0.0) * (selectedProduct?.defaultUnitPrice ?: 0.0)
    val total: Double get() = subtotal + depositCharged

    val canCreateNewCustomer: Boolean get() = newCustomerName.isNotBlank()
    val canContinueFromCliente: Boolean get() = selectedCustomer != null
    val canContinueFromProducto: Boolean get() = selectedProduct != null
    val canContinueFromCantidad: Boolean get() = (quantity ?: 0.0) > 0.0 && selectedPackagingId != null
}
