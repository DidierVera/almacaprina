package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PackagingMovementType {
    @SerialName("purchase") PURCHASE,
    @SerialName("sale_use") SALE_USE,
    @SerialName("waste") WASTE
}

@Serializable
data class PackagingInventory(
    val id: String,
    val date: LocalDate,
    @SerialName("packaging_id") val packagingId: String,
    @SerialName("movement_type") val movementType: PackagingMovementType,
    val quantity: Int,
    @SerialName("unit_cost") val unitCost: Double? = null
)
