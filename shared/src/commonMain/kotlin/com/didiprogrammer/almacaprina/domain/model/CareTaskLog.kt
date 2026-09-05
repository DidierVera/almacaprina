package com.didiprogrammer.almacaprina.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CareTaskLog(
    val id: String,
    @SerialName("care_task_id") val careTaskId: String,
    val date: LocalDate,
    val completed: Boolean,
    @SerialName("actual_quantity_used") val actualQuantityUsed: Double? = null,
    @SerialName("linked_record_id") val linkedRecordId: String? = null,
    val notes: String? = null
)
