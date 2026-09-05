package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CareTaskType {
    @SerialName("milking") MILKING,
    @SerialName("feeding") FEEDING,
    @SerialName("medication") MEDICATION,
    @SerialName("weighing") WEIGHING,
    @SerialName("other") OTHER
}

@Serializable
enum class CareTaskFrequency {
    @SerialName("daily") DAILY,
    @SerialName("specific_days") SPECIFIC_DAYS,
    @SerialName("weekly") WEEKLY,
    @SerialName("one_time") ONE_TIME
}

@Serializable
enum class CareTaskAnimalGroup {
    @SerialName("lactating") LACTATING,
    @SerialName("pregnant") PREGNANT,
    @SerialName("young_does") YOUNG_DOES,
    @SerialName("dry") DRY,
    @SerialName("breeding_bucks") BREEDING_BUCKS,
    @SerialName("general") GENERAL,
    @SerialName("all") ALL
}

@Serializable
enum class TimeOfDay {
    @SerialName("morning") MORNING,
    @SerialName("afternoon") AFTERNOON,
    @SerialName("both") BOTH,
    @SerialName("any") ANY
}

@Serializable
data class CareTask(
    val id: String,
    val name: String,
    @SerialName("task_type") val taskType: CareTaskType,
    val frequency: CareTaskFrequency,
    @SerialName("animal_group") val animalGroup: CareTaskAnimalGroup? = null,
    @SerialName("insumo_id") val insumoId: String? = null,
    @SerialName("quantity_per_occurrence") val quantityPerOccurrence: Double? = null,
    @SerialName("time_of_day") val timeOfDay: TimeOfDay? = null,
    val active: Boolean = true
)
