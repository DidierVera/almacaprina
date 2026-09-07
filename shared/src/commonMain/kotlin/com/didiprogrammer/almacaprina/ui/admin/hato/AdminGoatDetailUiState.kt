package com.didiprogrammer.almacaprina.ui.admin.hato

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import kotlinx.datetime.LocalDate

enum class GoatDetailTab(val label: String) {
    DATOS_BASICOS("Datos básicos"),
    PESO("Peso"),
    REPRODUCCION("Reproducción"),
    SALUD("Salud"),
    PRODUCCION_LECHE("Producción de leche")
}

data class AdminGoatDetailUiState(
    val isLoading: Boolean = true,
    val goat: Goat? = null,
    val ageLabel: String = "",
    val motherName: String? = null,
    val fatherName: String? = null,
    val lactationNumber: Int = 0,
    val selectedTab: GoatDetailTab = GoatDetailTab.DATOS_BASICOS,
    val weightRecords: List<WeightRecord> = emptyList(),
    val reproductiveEvents: List<ReproductiveEvent> = emptyList(),
    val nextExpectedBirth: LocalDate? = null,
    val healthRecords: List<HealthRecord> = emptyList(),
    val milkRecords: List<MilkProductionRecord> = emptyList(),
    val availableBucks: List<Goat> = emptyList(),
    val availableDoes: List<Goat> = emptyList(),
    val veterinaryInsumos: List<Insumo> = emptyList(),
    val savingAction: Boolean = false,
    val errorMessage: String? = null
) {
    val hasMilkHistory: Boolean get() = milkRecords.isNotEmpty()
}
