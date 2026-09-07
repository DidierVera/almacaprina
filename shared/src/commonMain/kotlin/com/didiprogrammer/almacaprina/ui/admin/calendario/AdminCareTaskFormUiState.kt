package com.didiprogrammer.almacaprina.ui.admin.calendario

import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay

data class AdminCareTaskFormUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val taskType: CareTaskType = CareTaskType.FEEDING,
    val frequency: CareTaskFrequency = CareTaskFrequency.DAILY,
    val animalGroup: CareTaskAnimalGroup? = null,
    val insumoId: String? = null,
    val quantityPerOccurrenceText: String = "",
    val timeOfDay: TimeOfDay? = null,
    val active: Boolean = true,
    val insumos: List<Insumo> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    /** Ordeño y pesada son solo recordatorios hacia flujos existentes — no consumen insumo. */
    val needsInsumo: Boolean
        get() = taskType == CareTaskType.FEEDING || taskType == CareTaskType.MEDICATION

    val quantityPerOccurrence: Double? get() = quantityPerOccurrenceText.toDoubleOrNull()

    val isValid: Boolean get() = name.isNotBlank()
}
