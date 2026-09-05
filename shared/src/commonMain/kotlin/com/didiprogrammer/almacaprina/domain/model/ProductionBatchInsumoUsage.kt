package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductionBatchInsumoUsage(
    val id: String,
    @SerialName("production_batch_id") val productionBatchId: String,
    @SerialName("insumo_id") val insumoId: String,
    @SerialName("quantity_used") val quantityUsed: Double,
    @SerialName("unit_cost_at_time") val unitCostAtTime: Double
    // cost es calculado (quantity_used * unit_cost_at_time) — ver business/
)
