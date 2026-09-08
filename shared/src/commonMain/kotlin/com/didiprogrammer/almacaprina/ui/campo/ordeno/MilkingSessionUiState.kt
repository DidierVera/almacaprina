package com.didiprogrammer.almacaprina.ui.campo.ordeno

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.NoMilkingReason

data class MilkingGoatEntry(
    val goat: Goat,
    val lactationNumber: Int,
    val sessionLiters: Double? = null,
    val noMilkingReason: NoMilkingReason? = null
) {
    val registered: Boolean get() = sessionLiters != null || noMilkingReason != null
}

/** Sesión de ordeño (mañana/tarde) — comparte una sola instancia entre las 3 pantallas de `campo/ordeno`. */
data class MilkingSessionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEveningSession: Boolean = false,
    val entries: List<MilkingGoatEntry> = emptyList(),
    val selectedGoatId: String? = null,
    val currentLitersText: String = "0",
    val useNumericKeypad: Boolean = false,
    val showReasonPicker: Boolean = false
) {
    val registeredCount: Int get() = entries.count { it.registered }
    val totalCount: Int get() = entries.size
    val allHandled: Boolean get() = entries.isNotEmpty() && entries.all { it.registered }
    val selectedEntry: MilkingGoatEntry? get() = entries.firstOrNull { it.goat.id == selectedGoatId }
    val totalLiters: Double get() = entries.sumOf { it.sessionLiters ?: 0.0 }
    val unmilkedCount: Int get() = entries.count { it.noMilkingReason != null }
    val currentLitersValue: Double get() = currentLitersText.replace(',', '.').toDoubleOrNull() ?: 0.0
}
