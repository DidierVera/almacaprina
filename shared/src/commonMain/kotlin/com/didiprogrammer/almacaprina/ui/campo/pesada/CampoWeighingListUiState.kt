package com.didiprogrammer.almacaprina.ui.campo.pesada

import com.didiprogrammer.almacaprina.domain.model.Goat

/** Campo · Pesada — lista de cabras con pesada vencida (reutiliza `overdueWeighings`). */
data class CampoWeighingListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val overdueGoats: List<Goat> = emptyList()
)
