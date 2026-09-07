package com.didiprogrammer.almacaprina.ui.campo.pesada

import com.didiprogrammer.almacaprina.domain.model.Goat
import kotlinx.datetime.LocalDate

data class CampoWeighingEntryUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val goat: Goat? = null,
    val date: LocalDate? = null,
    val weightText: String = "",
    val bodyConditionScore: Int? = null,
    val notes: String = ""
) {
    val weight: Double? get() = weightText.toDoubleOrNull()
    val isValid: Boolean get() = (weight ?: 0.0) > 0.0
}
