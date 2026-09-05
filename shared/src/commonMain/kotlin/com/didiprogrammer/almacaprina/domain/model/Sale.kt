package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod {
    @SerialName("cash") CASH,
    @SerialName("transfer") TRANSFER,
    @SerialName("other") OTHER
}

@Serializable
enum class PaymentStatus {
    @SerialName("paid") PAID,
    @SerialName("pending") PENDING
}

@Serializable
data class Sale(
    val id: String,
    val date: LocalDate,
    @SerialName("customer_id") val customerId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity_sold") val quantitySold: Double,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("packaging_id") val packagingId: String? = null,
    @SerialName("new_packaging_units_count") val newPackagingUnitsCount: Int? = null,
    @SerialName("packaging_returned_count") val packagingReturnedCount: Int? = null,
    @SerialName("payment_method") val paymentMethod: PaymentMethod? = null,
    @SerialName("payment_status") val paymentStatus: PaymentStatus,
    @SerialName("paid_date") val paidDate: LocalDate? = null,
    val notes: String? = null
    // deposit_charged, deposit_refunded y total_value son calculados — ver business/
)
