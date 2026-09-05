package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GoatSex {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE
}

@Serializable
enum class GoatStatus {
    @SerialName("kid") KID,
    @SerialName("young_doe") YOUNG_DOE,
    @SerialName("in_production") IN_PRODUCTION,
    @SerialName("pregnant") PREGNANT,
    @SerialName("dry") DRY,
    @SerialName("breeding_buck") BREEDING_BUCK,
    @SerialName("retired") RETIRED,
    @SerialName("deceased") DECEASED
}

@Serializable
enum class GoatOrigin {
    @SerialName("born_on_farm") BORN_ON_FARM,
    @SerialName("purchased") PURCHASED
}

@Serializable
data class Goat(
    val id: String,
    @SerialName("tag_number") val tagNumber: String,
    val name: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val sex: GoatSex,
    val breed: String? = null,
    @SerialName("breed_cross") val breedCross: String? = null,
    @SerialName("birth_date") val birthDate: LocalDate,
    @SerialName("mother_id") val motherId: String? = null,
    @SerialName("father_id") val fatherId: String? = null,
    @SerialName("external_father_description") val externalFatherDescription: String? = null,
    @SerialName("current_status") val currentStatus: GoatStatus,
    @SerialName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerialName("current_body_condition_score") val currentBodyConditionScore: Int? = null,
    @SerialName("herd_entry_date") val herdEntryDate: LocalDate,
    val origin: GoatOrigin,
    @SerialName("exit_date") val exitDate: LocalDate? = null,
    @SerialName("exit_reason") val exitReason: String? = null,
    val notes: String? = null
)
