package com.didiprogrammer.almacaprina.ui.campo.checklist

import com.didiprogrammer.almacaprina.business.DailyCareChecklist
import com.didiprogrammer.almacaprina.business.MilkingSessionProgress
import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Insumo

/** Campo · Checklist de hoy (Home de Campo). Ver mockups Campo Checklist-selection*.png. */
data class CampoChecklistUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val checklist: DailyCareChecklist = DailyCareChecklist(
        totalCount = 0,
        completedCount = 0,
        milkingTask = null,
        milkingProgress = MilkingSessionProgress(0, 0),
        otherCareTasks = emptyList(),
        healthReminders = emptyList()
    ),
    val insumosById: Map<String, Insumo> = emptyMap(),
    val isSaving: Boolean = false,
    val confirmingCareTask: CareTask? = null,
    val confirmQuantityText: String = "",
    val confirmingHealthReminder: HealthRecord? = null,
    val confirmHealthQuantityText: String = ""
)
