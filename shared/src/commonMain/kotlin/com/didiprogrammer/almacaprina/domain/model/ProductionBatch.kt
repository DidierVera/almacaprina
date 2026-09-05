package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductionBatch(
    val id: String,
    val date: LocalDate,
    @SerialName("output_product_id") val outputProductId: String,
    @SerialName("milk_liters_used") val milkLitersUsed: Double,
    @SerialName("output_quantity") val outputQuantity: Double,
    val responsible: String? = null,
    val notes: String? = null
    // yield_ratio es calculado (milk_liters_used / output_quantity) — ver business/
)
