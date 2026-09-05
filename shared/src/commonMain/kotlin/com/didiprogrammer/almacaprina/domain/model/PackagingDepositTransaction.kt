package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DepositMovementType {
    @SerialName("deposit_charged") DEPOSIT_CHARGED,
    @SerialName("deposit_returned") DEPOSIT_RETURNED
}

@Serializable
data class PackagingDepositTransaction(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("sale_id") val saleId: String,
    @SerialName("packaging_id") val packagingId: String,
    val date: LocalDate,
    @SerialName("movement_type") val movementType: DepositMovementType,
    val quantity: Int
    // amount es calculado (quantity * Packaging.deposit_amount) — ver business/
)
