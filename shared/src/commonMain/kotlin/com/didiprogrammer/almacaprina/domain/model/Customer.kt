package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomerType {
    @SerialName("individual") INDIVIDUAL,
    @SerialName("business") BUSINESS
}

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val type: CustomerType,
    val contact: String? = null,
    val address: String? = null,
    val notes: String? = null
)
