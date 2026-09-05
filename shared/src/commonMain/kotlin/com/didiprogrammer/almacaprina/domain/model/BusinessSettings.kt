package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessSettings(
    val id: String,
    @SerialName("farm_name") val farmName: String,
    val currency: String,
    @SerialName("target_daily_liters_goal") val targetDailyLitersGoal: Double,
    @SerialName("deposit_alert_days") val depositAlertDays: Int,
    @SerialName("updated_at") val updatedAt: LocalDate? = null
)
