package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NoMilkingReason {
    @SerialName("dry") DRY,
    @SerialName("sick") SICK,
    @SerialName("under_treatment") UNDER_TREATMENT,
    @SerialName("other") OTHER
}

@Serializable
data class MilkProductionRecord(
    val id: String,
    @SerialName("goat_id") val goatId: String,
    val date: LocalDate,
    @SerialName("morning_milking_liters") val morningMilkingLiters: Double? = null,
    @SerialName("evening_milking_liters") val eveningMilkingLiters: Double? = null,
    @SerialName("fat_pct") val fatPct: Double? = null,
    @SerialName("protein_pct") val proteinPct: Double? = null,
    @SerialName("no_milking_reason") val noMilkingReason: NoMilkingReason? = null,
    @SerialName("no_milking_reason_detail") val noMilkingReasonDetail: String? = null
    // total_liters_day y days_in_milk son calculados — ver business/
)
