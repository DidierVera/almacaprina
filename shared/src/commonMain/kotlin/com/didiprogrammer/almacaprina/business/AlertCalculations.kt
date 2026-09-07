package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Vacunas/desparasitaciones/etc. con next_suggested_date dentro de la ventana [today, today+withinDays]. */
fun upcomingHealthAlerts(
    records: List<HealthRecord>,
    today: LocalDate,
    withinDays: Int = DEFAULT_ALERT_WINDOW_DAYS
): List<HealthRecord> =
    records
        .filter { record ->
            val next = record.nextSuggestedDate ?: return@filter false
            val daysUntilNext = today.daysUntil(next)
            daysUntilNext in 0..withinDays
        }
        .sortedBy { it.nextSuggestedDate }

/**
 * Cabras activas cuya última pesada tiene más de [cadenceDays] días (o nunca han sido pesadas).
 * Se consideran solo cabras que ya deberían estar en el ciclo de pesadas (excluye cabritos).
 */
fun overdueWeighings(
    activeGoats: List<Goat>,
    weightRecords: List<WeightRecord>,
    today: LocalDate,
    cadenceDays: Int = WEIGHING_CADENCE_DAYS
): List<Goat> {
    val lastWeighingByGoat = weightRecords
        .groupBy { it.goatId }
        .mapValues { (_, records) -> records.maxOf { it.date } }

    return activeGoats
        .filter { it.exitDate == null && it.currentStatus != GoatStatus.KID }
        .filter { goat ->
            val lastDate = lastWeighingByGoat[goat.id]
            lastDate == null || lastDate.daysUntil(today) > cadenceDays
        }
}

/** Eventos de monta (breeding) pendientes cuyo parto esperado cae en [today, today+withinDays]. */
fun upcomingBirths(
    events: List<ReproductiveEvent>,
    today: LocalDate,
    withinDays: Int = DEFAULT_ALERT_WINDOW_DAYS
): List<ReproductiveEvent> =
    events
        .filter { it.eventType == ReproductiveEventType.BREEDING && it.result == ReproductiveEventResult.PENDING }
        .filter { event ->
            val expected = event.date.expectedBirthDate()
            val daysUntilBirth = today.daysUntil(expected)
            daysUntilBirth in 0..withinDays
        }
        .sortedBy { it.date.expectedBirthDate() }

/** InsumoStockBalance — existencias reales de un insumo (compras − consumo registrado). */
fun insumoStockBalance(
    insumoId: String,
    purchases: List<Purchase>,
    feedingRecords: List<FeedingRecord>,
    healthRecords: List<HealthRecord>,
    batchUsages: List<ProductionBatchInsumoUsage>
): Double {
    val purchased = purchases.filter { it.insumoId == insumoId }.sumOf { it.quantity }
    val fedOut = feedingRecords.filter { it.insumoId == insumoId }.sumOf { it.quantity }
    val usedInHealth = healthRecords.filter { it.insumoId == insumoId }.sumOf { it.quantityUsed ?: 0.0 }
    val usedInBatches = batchUsages.filter { it.insumoId == insumoId }.sumOf { it.quantityUsed }
    return purchased - fedOut - usedInHealth - usedInBatches
}

/** InsumoPlannedDailyConsumption — consumo diario esperado según CareTask activas diarias. */
fun insumoPlannedDailyConsumption(insumoId: String, activeCareTasks: List<CareTask>): Double =
    activeCareTasks
        .filter { it.active && it.frequency == CareTaskFrequency.DAILY && it.insumoId == insumoId }
        .sumOf { it.quantityPerOccurrence ?: 0.0 }

/** InsumoDaysRemaining — días de existencias al ritmo de consumo planeado. Null si no hay consumo planeado. */
fun insumoDaysRemaining(stockBalance: Double, plannedDailyConsumption: Double): Double? =
    if (plannedDailyConsumption <= 0.0) null else stockBalance / plannedDailyConsumption

/** Un insumo con pocos días de existencia respecto a su tiempo de reabastecimiento. */
data class InsumoLowStockAlert(val insumo: Insumo, val daysRemaining: Double)

/**
 * Insumos activos donde InsumoDaysRemaining ≤ Insumo.reorder_lead_time_days
 * (proyección basada en el calendario configurado, no un umbral fijo de cantidad).
 */
fun insumoLowStockAlerts(
    insumos: List<Insumo>,
    purchases: List<Purchase>,
    feedingRecords: List<FeedingRecord>,
    healthRecords: List<HealthRecord>,
    batchUsages: List<ProductionBatchInsumoUsage>,
    activeCareTasks: List<CareTask>
): List<InsumoLowStockAlert> =
    insumos
        .filter { it.active }
        .mapNotNull { insumo ->
            val leadTime = insumo.reorderLeadTimeDays ?: return@mapNotNull null
            val stock = insumoStockBalance(insumo.id, purchases, feedingRecords, healthRecords, batchUsages)
            val plannedDaily = insumoPlannedDailyConsumption(insumo.id, activeCareTasks)
            val daysRemaining = insumoDaysRemaining(stock, plannedDaily) ?: return@mapNotNull null
            if (daysRemaining <= leadTime) InsumoLowStockAlert(insumo, daysRemaining) else null
        }
        .sortedBy { it.daysRemaining }
