package com.didiprogrammer.almacaprina.ui.campo.cabras

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecord

/** Campo · historial de salud de una cabra, de solo lectura — mismos HealthRecord que ve
 * Admin en la ficha técnica (los que Campo reportó desde "Reportar novedad" y los de Admin). */
data class CampoGoatHealthDetailUiState(
    val isLoading: Boolean = true,
    val goat: Goat? = null,
    val healthRecords: List<HealthRecord> = emptyList(),
    val errorMessage: String? = null
)
