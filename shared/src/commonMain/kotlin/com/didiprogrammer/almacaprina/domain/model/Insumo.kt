package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InsumoCategory {
    @SerialName("feed") FEED,
    @SerialName("veterinary") VETERINARY,
    @SerialName("processing_input") PROCESSING_INPUT,
    @SerialName("transport") TRANSPORT,
    @SerialName("labor") LABOR,
    @SerialName("maintenance") MAINTENANCE,
    @SerialName("other") OTHER
}

@Serializable
enum class UnitOfMeasure {
    @SerialName("kg") KG,
    @SerialName("g") G,
    @SerialName("liter") LITER,
    @SerialName("ml") ML,
    @SerialName("unit") UNIT
}

@Serializable
data class Insumo(
    val id: String,
    val name: String,
    val category: InsumoCategory,
    @SerialName("unit_of_measure") val unitOfMeasure: UnitOfMeasure,
    @SerialName("last_unit_cost") val lastUnitCost: Double? = null,
    @SerialName("reorder_lead_time_days") val reorderLeadTimeDays: Int? = null,
    val active: Boolean = true,
    /** Nombre del empaque en que se compra (ej. "Botella", "Bulto", "Saco"). Opcional. */
    @SerialName("purchase_package_label") val purchasePackageLabel: String? = null,
    /** Contenido de un empaque, en `unitOfMeasure` (ej. 50 para una botella de 50 ml). */
    @SerialName("purchase_package_size") val purchasePackageSize: Double? = null,
    val notes: String? = null
)
