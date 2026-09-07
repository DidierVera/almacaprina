package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ProductionBatch
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import com.didiprogrammer.almacaprina.domain.model.Sale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** total_liters_day (calculado) de un único registro de ordeño. */
fun MilkProductionRecord.totalLitersDay(): Double =
    (morningMilkingLiters ?: 0.0) + (eveningMilkingLiters ?: 0.0)

/** DailyHerdProduction — suma de total_liters_day de todas las cabras, para una fecha dada. */
fun dailyHerdProduction(records: List<MilkProductionRecord>, date: LocalDate): Double =
    records.filter { it.date == date }.sumOf { it.totalLitersDay() }

/**
 * RawMilkAvailableBalance — litros de leche cruda disponibles:
 * Σ DailyHerdProduction (histórico) − Σ Sale.quantity_sold (producto = leche) − Σ ProductionBatch.milk_liters_used
 */
fun rawMilkAvailableBalance(
    milkRecords: List<MilkProductionRecord>,
    sales: List<Sale>,
    milkProductId: String,
    batches: List<ProductionBatch>
): Double {
    val totalProduced = milkRecords.sumOf { it.totalLitersDay() }
    val totalSoldAsMilk = sales.filter { it.productId == milkProductId }.sumOf { it.quantitySold }
    val totalUsedInBatches = batches.sumOf { it.milkLitersUsed }
    return totalProduced - totalSoldAsMilk - totalUsedInBatches
}

/** yield_ratio (calculado) = milk_liters_used / output_quantity. Null si output_quantity es 0. */
fun ProductionBatch.yieldRatio(): Double? =
    if (outputQuantity == 0.0) null else milkLitersUsed / outputQuantity

/** ProductYieldRatio — promedio histórico de yield_ratio de un producto derivado. */
fun averageYieldRatio(batches: List<ProductionBatch>, productId: String): Double? {
    val ratios = batches.filter { it.outputProductId == productId }.mapNotNull { it.yieldRatio() }
    return if (ratios.isEmpty()) null else ratios.average()
}

/** expected_birth_date (calculado) = fecha de breeding + 150 días de gestación. */
fun LocalDate.expectedBirthDate(): LocalDate = this.plus(GOAT_GESTATION_DAYS, kotlinx.datetime.DateTimeUnit.DAY)

/** Solo tiene sentido para eventos de tipo breeding — devuelve null en cualquier otro caso. */
fun expectedBirthDateOrNull(eventType: ReproductiveEventType, breedingDate: LocalDate): LocalDate? =
    if (eventType == ReproductiveEventType.BREEDING) breedingDate.expectedBirthDate() else null
