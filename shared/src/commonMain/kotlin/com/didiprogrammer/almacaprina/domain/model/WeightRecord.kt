package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeightRecord(
    val id: String,
    @SerialName("goat_id") val goatId: String,
    val date: LocalDate,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("body_condition_score") val bodyConditionScore: Int? = null,
    val notes: String? = null
)
