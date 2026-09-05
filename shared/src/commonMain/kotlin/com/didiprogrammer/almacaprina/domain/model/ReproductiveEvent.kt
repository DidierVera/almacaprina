package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReproductiveEventType {
    @SerialName("heat_detected") HEAT_DETECTED,
    @SerialName("breeding") BREEDING,
    @SerialName("pregnancy_diagnosis") PREGNANCY_DIAGNOSIS,
    @SerialName("birth") BIRTH,
    @SerialName("abortion") ABORTION
}

@Serializable
enum class ReproductiveEventResult {
    @SerialName("pending") PENDING,
    @SerialName("successful") SUCCESSFUL,
    @SerialName("failed") FAILED
}

@Serializable
data class ReproductiveEvent(
    val id: String,
    @SerialName("doe_id") val doeId: String,
    @SerialName("event_type") val eventType: ReproductiveEventType,
    val date: LocalDate,
    @SerialName("buck_id") val buckId: String? = null,
    val result: ReproductiveEventResult,
    @SerialName("kids_born_count") val kidsBornCount: Int? = null,
    @SerialName("kids_alive_count") val kidsAliveCount: Int? = null,
    @SerialName("kid_ids") val kidIds: List<String>? = null,
    val notes: String? = null
    // expected_birth_date es calculado (date de breeding + 150 días) — ver business/ en el próximo paso
)
