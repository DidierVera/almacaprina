package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Packaging(
    val id: String,
    val name: String,
    @SerialName("is_returnable") val isReturnable: Boolean,
    @SerialName("deposit_amount") val depositAmount: Double? = null,
    @SerialName("unit_cost") val unitCost: Double
)
