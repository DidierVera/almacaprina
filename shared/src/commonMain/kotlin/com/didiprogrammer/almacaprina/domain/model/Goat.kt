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

/** Un componente de la composición racial de una cabra (ej. {"Alpina", 50.0}). */
@Serializable
data class BreedPercentage(
    @SerialName("breed_name") val breedName: String,
    val percentage: Double
)

@Serializable
data class Goat(
    val id: String,
    @SerialName("tag_number") val tagNumber: String,
    val name: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val sex: GoatSex,
    /** Puede ser 100% de una raza o una mezcla — ver [com.didiprogrammer.almacaprina.business.averageBreedComposition]. */
    @SerialName("breed_composition") val breedComposition: List<BreedPercentage> = emptyList(),
    @SerialName("birth_date") val birthDate: LocalDate,
    @SerialName("mother_id") val motherId: String? = null,
    @SerialName("father_id") val fatherId: String? = null,
    @SerialName("external_father_description") val externalFatherDescription: String? = null,
    /** Solo tiene sentido junto con [externalFatherDescription] — permite calcular breed_composition
     * de la cría aunque el padre no esté en el hato (ver business.averageBreedComposition). */
    @SerialName("external_father_breed_composition") val externalFatherBreedComposition: List<BreedPercentage>? = null,
    @SerialName("current_status") val currentStatus: GoatStatus,
    @SerialName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerialName("current_body_condition_score") val currentBodyConditionScore: Int? = null,
    @SerialName("herd_entry_date") val herdEntryDate: LocalDate,
    /** Fecha en que la cría deja de tomar leche de la madre. */
    @SerialName("weaning_date") val weaningDate: LocalDate? = null,
    val origin: GoatOrigin,
    @SerialName("exit_date") val exitDate: LocalDate? = null,
    @SerialName("exit_reason") val exitReason: String? = null,
    val notes: String? = null
)
