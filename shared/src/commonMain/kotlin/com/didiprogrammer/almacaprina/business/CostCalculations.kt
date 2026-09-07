package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.Sale

/** total_cost (calculado) = quantity * unit_cost. */
fun Purchase.totalCost(): Double = quantity * unitCost

/** cost (calculado) = quantity_used * unit_cost_at_time. */
fun ProductionBatchInsumoUsage.cost(): Double = quantityUsed * unitCostAtTime

/**
 * CostPerLiter — Σ costos (Purchase + HealthRecord.cost + FeedingRecord.cost) / litros producidos,
 * por período. Sin deduplicación entre fuentes (confirmado con el dueño — ver CLAUDE.md).
 * Devuelve null si no hay litros producidos en el período (evita división por cero).
 */
fun costPerLiter(
    purchases: List<Purchase>,
    healthRecords: List<HealthRecord>,
    feedingRecords: List<FeedingRecord>,
    litersProducedInPeriod: Double
): Double? {
    if (litersProducedInPeriod <= 0.0) return null
    val totalCost = purchases.sumOf { it.totalCost() } +
        healthRecords.sumOf { it.cost ?: 0.0 } +
        feedingRecords.sumOf { it.cost ?: 0.0 }
    return totalCost / litersProducedInPeriod
}

/** deposit_charged (calculado) = new_packaging_units_count * Packaging.deposit_amount. */
fun Sale.depositCharged(packaging: Packaging?): Double =
    (newPackagingUnitsCount ?: 0) * (packaging?.depositAmount ?: 0.0)

/** deposit_refunded (calculado) = packaging_returned_count * Packaging.deposit_amount. */
fun Sale.depositRefunded(packaging: Packaging?): Double =
    (packagingReturnedCount ?: 0) * (packaging?.depositAmount ?: 0.0)

/** total_value (calculado) = (quantity_sold * unit_price) + deposit_charged - deposit_refunded. */
fun Sale.totalValue(packaging: Packaging?): Double =
    (quantitySold * unitPrice) + depositCharged(packaging) - depositRefunded(packaging)

/** Ingreso real del negocio por una venta — excluye depósitos (no son ingreso hasta que se devuelven). */
fun Sale.revenue(): Double = quantitySold * unitPrice
