package com.didiprogrammer.almacaprina.ui.admin.compras

import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.ui.components.toInsumoCategoryOrNull
import kotlinx.datetime.LocalDate

data class AdminNewPurchaseUiState(
    val isLoading: Boolean = true,
    val category: PurchaseCategory = PurchaseCategory.FEED,
    val insumos: List<Insumo> = emptyList(),
    val packagings: List<Packaging> = emptyList(),
    val selectedInsumoId: String? = null,
    val selectedPackagingId: String? = null,
    val date: LocalDate? = null,
    val supplier: String = "",
    val quantityText: String = "",
    val unitCostText: String = "",
    /** Compra "por empaque" (ej. 2 botellas) en vez de cantidad/costo directos en unit_of_measure. */
    val purchaseByPackage: Boolean = false,
    val packageCountText: String = "",
    val packageCostText: String = "",
    val notes: String = "",
    val currency: String = "COP",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedInsumo: Insumo? get() = insumos.firstOrNull { it.id == selectedInsumoId }

    /** Solo tiene sentido para insumos (no envases) con un empaque de compra definido en el catálogo. */
    val packageSize: Double? get() = selectedInsumo?.purchasePackageSize?.takeIf { it > 0.0 }
    val hasPackageOption: Boolean get() = category != PurchaseCategory.PACKAGING && packageSize != null

    private val packageCount: Double? get() = packageCountText.toDoubleOrNull()
    private val packageUnitCost: Double? get() = packageCostText.toDoubleOrNull()

    val quantity: Double?
        get() = if (purchaseByPackage && hasPackageOption) {
            val count = packageCount ?: return null
            val size = packageSize ?: return null
            count * size
        } else {
            quantityText.toDoubleOrNull()
        }

    val unitCost: Double?
        get() = if (purchaseByPackage && hasPackageOption) {
            val cost = packageUnitCost ?: return null
            val size = packageSize ?: return null
            cost / size
        } else {
            unitCostText.toDoubleOrNull()
        }

    val totalCost: Double get() = (quantity ?: 0.0) * (unitCost ?: 0.0)

    /** Insumos filtrados al rubro de la categoría elegida (ver PurchaseCategory.toInsumoCategoryOrNull). */
    val filteredInsumos: List<Insumo>
        get() = category.toInsumoCategoryOrNull()?.let { ic -> insumos.filter { it.category == ic } } ?: emptyList()

    val isValid: Boolean
        get() = date != null &&
            (quantity ?: 0.0) > 0.0 &&
            (unitCost ?: -1.0) >= 0.0 &&
            if (category == PurchaseCategory.PACKAGING) selectedPackagingId != null else selectedInsumoId != null
}
