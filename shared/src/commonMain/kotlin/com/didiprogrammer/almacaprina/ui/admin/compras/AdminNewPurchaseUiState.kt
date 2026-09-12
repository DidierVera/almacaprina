package com.didiprogrammer.almacaprina.ui.admin.compras

import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.ui.components.toInsumoCategoryOrNull
import kotlinx.datetime.LocalDate

data class AdminNewPurchaseUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
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
    /** % de IVA a sumar sobre el subtotal — mutuamente excluyente con [vatIncluded]. */
    val vatPercentageText: String = "",
    /** El unit_cost ingresado ya incluye IVA — no se suma nada extra. */
    val vatIncluded: Boolean = false,
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

    /** Nulo = sin % de IVA cargado. Ignorado si [vatIncluded] es true. */
    val vatPercentage: Double? get() = if (vatIncluded) null else vatPercentageText.toDoubleOrNull()

    val totalCost: Double
        get() {
            val subtotal = (quantity ?: 0.0) * (unitCost ?: 0.0)
            val vatPercentage = vatPercentage
            return if (vatIncluded || vatPercentage == null) subtotal else subtotal * (1 + vatPercentage / 100)
        }

    /** Insumos filtrados al rubro de la categoría elegida (ver PurchaseCategory.toInsumoCategoryOrNull). */
    val filteredInsumos: List<Insumo>
        get() = category.toInsumoCategoryOrNull()?.let { ic -> insumos.filter { it.category == ic } } ?: emptyList()

    /** Mano de obra y Otro son gastos, no algo que se consuma de un catálogo con stock —
     * no tiene sentido forzar seleccionar un insumo para esas dos categorías. */
    val requiresInsumo: Boolean
        get() = category != PurchaseCategory.PACKAGING && category != PurchaseCategory.LABOR && category != PurchaseCategory.OTHER

    private val vatPercentageIsValid: Boolean
        get() = vatIncluded || vatPercentageText.isBlank() || (vatPercentageText.toDoubleOrNull()?.let { it >= 0.0 } == true)

    val isValid: Boolean
        get() = date != null &&
            (quantity ?: 0.0) > 0.0 &&
            (unitCost ?: -1.0) >= 0.0 &&
            vatPercentageIsValid &&
            when (category) {
                PurchaseCategory.PACKAGING -> selectedPackagingId != null
                PurchaseCategory.LABOR, PurchaseCategory.OTHER -> true
                else -> selectedInsumoId != null
            }
}
