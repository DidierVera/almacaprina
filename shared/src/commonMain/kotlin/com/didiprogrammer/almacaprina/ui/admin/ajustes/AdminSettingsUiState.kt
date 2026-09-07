package com.didiprogrammer.almacaprina.ui.admin.ajustes

/** Sección "Más · Ajustes". Ver CLAUDE.md § Configuración. */
data class AdminSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val settingsId: String? = null,
    val farmName: String = "",
    val currency: String = "COP",
    val targetDailyLitersGoalText: String = "",
    val depositAlertDaysText: String = "",
    /** CostPerLiter de los últimos 30 días — SIEMPRE de solo lectura, nunca editable (ver CLAUDE.md). */
    val costPerLiter: Double? = null,
    val hasPin: Boolean = false
) {
    val isValid: Boolean
        get() = farmName.isNotBlank() &&
            currency.isNotBlank() &&
            targetDailyLitersGoalText.toDoubleOrNull() != null &&
            depositAlertDaysText.toIntOrNull() != null
}
