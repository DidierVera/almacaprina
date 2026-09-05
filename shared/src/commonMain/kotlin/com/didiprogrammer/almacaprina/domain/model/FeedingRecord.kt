package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AnimalGroup {
    @SerialName("lactating") LACTATING,
    @SerialName("pregnant") PREGNANT,
    @SerialName("young_does") YOUNG_DOES,
    @SerialName("dry") DRY,
    @SerialName("breeding_bucks") BREEDING_BUCKS,
    @SerialName("general") GENERAL
}

@Serializable
data class FeedingRecord(
    val id: String,
    val date: LocalDate,
    @SerialName("animal_group") val animalGroup: AnimalGroup? = null,
    @SerialName("goat_id") val goatId: String? = null,
    @SerialName("insumo_id") val insumoId: String,
    val quantity: Double,
    val cost: Double? = null
)
