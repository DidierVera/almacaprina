package com.didiprogrammer.almacaprina.ui.campo.novedad

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import kotlinx.datetime.LocalDate

/**
 * Campo · Reportar novedad — formulario ligero para que Campo registre una observación
 * puntual de una cabra (ej. "cojea de la pata trasera") como HealthRecord, sin necesitar una
 * CareTask (que es solo para tareas recurrentes de grupo, ver CLAUDE.md).
 */
data class CampoReportNovedadUiState(
    val isLoading: Boolean = true,
    val allGoats: List<Goat> = emptyList(),
    val selectedGoat: Goat? = null,
    val date: LocalDate? = null,
    val type: HealthRecordType = HealthRecordType.DIAGNOSIS,
    val description: String = "",
    val nextSuggestedDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = selectedGoat != null && date != null && description.isNotBlank()
}
