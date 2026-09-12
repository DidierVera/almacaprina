package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PurchaseCategory {
    @SerialName("feed") FEED,
    @SerialName("veterinary") VETERINARY,
    @SerialName("packaging") PACKAGING,
    @SerialName("processing_input") PROCESSING_INPUT,
    @SerialName("transport") TRANSPORT,
    @SerialName("labor") LABOR,
    @SerialName("maintenance") MAINTENANCE,
    @SerialName("other") OTHER
}

@Serializable
data class Purchase(
    val id: String,
    val date: LocalDate,
    val category: PurchaseCategory,
    @SerialName("insumo_id") val insumoId: String? = null,
    @SerialName("packaging_id") val packagingId: String? = null,
    val supplier: String? = null,
    val quantity: Double,
    val unit: String? = null,
    @SerialName("unit_cost") val unitCost: Double,
    /** % de IVA a sumar sobre quantity*unit_cost. Mutuamente excluyente con [vatIncluded]. */
    @SerialName("vat_percentage") val vatPercentage: Double? = null,
    /** true si el unit_cost ingresado ya incluye IVA — no se suma nada extra. */
    @SerialName("vat_included") val vatIncluded: Boolean = false,
    val notes: String? = null
    // total_cost es calculado (quantity * unit_cost, + IVA si aplica) — ver business/
)
