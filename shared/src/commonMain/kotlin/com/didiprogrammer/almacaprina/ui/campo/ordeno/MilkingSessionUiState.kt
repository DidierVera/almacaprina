package com.didiprogrammer.almacaprina.ui.campo.ordeno

import com.didiprogrammer.almacaprina.business.MILLILITERS_PER_LITER
import com.didiprogrammer.almacaprina.business.MilkEntryUnit
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.NoMilkingReason

data class MilkingGoatEntry(
    val goat: Goat,
    val lactationNumber: Int,
    /** Lo registrado en esta sesión, siempre en ml (la unidad de captura no afecta lo guardado). */
    val sessionMl: Double? = null,
    val noMilkingReason: NoMilkingReason? = null
) {
    val registered: Boolean get() = sessionMl != null || noMilkingReason != null
}

/** Sesión de ordeño (mañana/tarde) — comparte una sola instancia entre las 3 pantallas de `campo/ordeno`. */
data class MilkingSessionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEveningSession: Boolean = false,
    val entries: List<MilkingGoatEntry> = emptyList(),
    val selectedGoatId: String? = null,
    /** Unidad en la que Campo está capturando (ml u oz) — se conserva entre cabras durante la sesión. */
    val entryUnit: MilkEntryUnit = MilkEntryUnit.MILLILITER,
    /** Número tal como lo ve el usuario, expresado en [entryUnit]. */
    val currentValueText: String = "0",
    val useNumericKeypad: Boolean = false,
    val showReasonPicker: Boolean = false
) {
    val registeredCount: Int get() = entries.count { it.registered }
    val totalCount: Int get() = entries.size
    val allHandled: Boolean get() = entries.isNotEmpty() && entries.all { it.registered }
    val selectedEntry: MilkingGoatEntry? get() = entries.firstOrNull { it.goat.id == selectedGoatId }
    val totalLiters: Double get() = entries.sumOf { it.sessionMl ?: 0.0 } / MILLILITERS_PER_LITER
    val unmilkedCount: Int get() = entries.count { it.noMilkingReason != null }
    val currentValue: Double get() = currentValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    /** Incremento del stepper en la unidad actual (50 ml o 0,5 oz). */
    val stepSize: Double get() = when (entryUnit) {
        MilkEntryUnit.MILLILITER -> 50.0
        MilkEntryUnit.OUNCE -> 0.5
    }
}
