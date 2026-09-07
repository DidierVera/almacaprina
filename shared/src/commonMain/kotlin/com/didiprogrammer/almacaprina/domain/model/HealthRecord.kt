package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class HealthRecordType {
    @SerialName("vaccine") VACCINE,
    @SerialName("deworming") DEWORMING,
    @SerialName("treatment") TREATMENT,
    @SerialName("routine_checkup") ROUTINE_CHECKUP,
    @SerialName("diagnosis") DIAGNOSIS
}

@Serializable
data class HealthRecord(
    val id: String,
    /** Nulo cuando el evento es a nivel de GRUPO (ej. una CareTask de medicación completada
     * desde el checklist de Campo) en vez de a una cabra puntual — ver CareTask.animal_group. */
    @SerialName("goat_id") val goatId: String? = null,
    val type: HealthRecordType,
    val date: LocalDate,
    val description: String? = null,
    @SerialName("insumo_id") val insumoId: String? = null,
    val dosage: String? = null,
    @SerialName("quantity_used") val quantityUsed: Double? = null,
    @SerialName("milk_withdrawal_days") val milkWithdrawalDays: Int? = null,
    val cost: Double? = null,
    val veterinarian: String? = null,
    @SerialName("next_suggested_date") val nextSuggestedDate: LocalDate? = null
)
