package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.goat_detail_tab_datos_basicos
import almacaprina.shared.generated.resources.goat_detail_tab_peso
import almacaprina.shared.generated.resources.goat_detail_tab_produccion_leche
import almacaprina.shared.generated.resources.goat_detail_tab_reproduccion
import almacaprina.shared.generated.resources.goat_detail_tab_salud
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

enum class GoatDetailTab {
    DATOS_BASICOS,
    PESO,
    REPRODUCCION,
    SALUD,
    PRODUCCION_LECHE
}

@Composable
fun GoatDetailTab.label(): String = stringResource(
    when (this) {
        GoatDetailTab.DATOS_BASICOS -> Res.string.goat_detail_tab_datos_basicos
        GoatDetailTab.PESO -> Res.string.goat_detail_tab_peso
        GoatDetailTab.REPRODUCCION -> Res.string.goat_detail_tab_reproduccion
        GoatDetailTab.SALUD -> Res.string.goat_detail_tab_salud
        GoatDetailTab.PRODUCCION_LECHE -> Res.string.goat_detail_tab_produccion_leche
    }
)

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
    val breeds: List<Breed> = emptyList(),
    val savingAction: Boolean = false,
    val errorMessage: String? = null
) {
    val hasMilkHistory: Boolean get() = milkRecords.isNotEmpty()
}
