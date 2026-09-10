package com.didiprogrammer.almacaprina.ui.campo.alertas

import com.didiprogrammer.almacaprina.business.ChecklistHealthReminder

/** Campo · alertas de salud pendientes (mismo recordatorio que aparece en el checklist diario,
 * ver CareChecklistCalculations.dailyHealthReminders) — pero navegables hacia la ficha técnica
 * de la cabra en vez de abrir el diálogo de "marcar como hecho". */
data class CampoAlertsUiState(
    val isLoading: Boolean = true,
    val reminders: List<ChecklistHealthReminder> = emptyList(),
    val errorMessage: String? = null
)
