package com.didiprogrammer.almacaprina.ui.admin.produccion

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.batch_wizard_step_cantidad
import almacaprina.shared.generated.resources.batch_wizard_step_confirmar
import almacaprina.shared.generated.resources.batch_wizard_step_insumos
import almacaprina.shared.generated.resources.batch_wizard_step_litros
import almacaprina.shared.generated.resources.batch_wizard_step_producto
import almacaprina.shared.generated.resources.batch_wizard_step_responsable
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.UnitOfMeasure
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

enum class BatchWizardStep {
    PRODUCTO,
    LITROS,
    CANTIDAD,
    INSUMOS,
    RESPONSABLE,
    CONFIRMAR
}

@Composable
fun BatchWizardStep.title(): String = stringResource(
    when (this) {
        BatchWizardStep.PRODUCTO -> Res.string.batch_wizard_step_producto
        BatchWizardStep.LITROS -> Res.string.batch_wizard_step_litros
        BatchWizardStep.CANTIDAD -> Res.string.batch_wizard_step_cantidad
        BatchWizardStep.INSUMOS -> Res.string.batch_wizard_step_insumos
        BatchWizardStep.RESPONSABLE -> Res.string.batch_wizard_step_responsable
        BatchWizardStep.CONFIRMAR -> Res.string.batch_wizard_step_confirmar
    }
)

/** Una fila editable de insumo usado en el lote — prellenada desde ProductRecipeItem. */
data class BatchInsumoUsageEntry(
    val insumoId: String,
    val insumoName: String,
    val unitOfMeasure: UnitOfMeasure?,
    val quantityUsed: Double,
    val unitCostAtTime: Double
) {
    val cost: Double get() = quantityUsed * unitCostAtTime
}

data class AdminNewBatchUiState(
    val isLoading: Boolean = true,
    val step: BatchWizardStep = BatchWizardStep.PRODUCTO,
    val derivedProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val availableMilkLiters: Double = 0.0,
    val milkLitersUsed: Double = 0.0,
    val outputQuantityText: String = "",
    val historicalAverageYield: Double? = null,
    val insumoUsages: List<BatchInsumoUsageEntry> = emptyList(),
    val allInsumos: List<Insumo> = emptyList(),
    val date: LocalDate? = null,
    val responsible: String = "",
    val notes: String = "",
    val costPerLiterMilk: Double? = null,
    val currency: String = "COP",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val outputQuantity: Double? get() = outputQuantityText.toDoubleOrNull()

    val currentYieldRatio: Double?
        get() = outputQuantity?.takeIf { it > 0 }?.let { milkLitersUsed / it }

    val milkExceedsAvailable: Boolean get() = milkLitersUsed > availableMilkLiters

    val insumosCost: Double get() = insumoUsages.sumOf { it.cost }

    val milkCost: Double get() = (costPerLiterMilk ?: 0.0) * milkLitersUsed

    val totalCost: Double get() = milkCost + insumosCost

    val canGoNext: Boolean
        get() = when (step) {
            BatchWizardStep.PRODUCTO -> selectedProduct != null
            BatchWizardStep.LITROS -> milkLitersUsed > 0.0
            BatchWizardStep.CANTIDAD -> (outputQuantity ?: 0.0) > 0.0
            BatchWizardStep.INSUMOS -> true
            BatchWizardStep.RESPONSABLE -> date != null
            BatchWizardStep.CONFIRMAR -> true
        }
}
