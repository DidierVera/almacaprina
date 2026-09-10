package com.didiprogrammer.almacaprina.ui.campo.cabras

import com.didiprogrammer.almacaprina.domain.model.Goat

/** Campo · lista completa del hato, de solo lectura — punto de entrada para ver el historial
 * de salud de cualquier cabra (ver CampoGoatHealthDetailScreen). */
data class CampoGoatListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allGoats: List<Goat> = emptyList(),
    val query: String = "",
    val errorMessage: String? = null
) {
    val filteredGoats: List<Goat>
        get() = allGoats
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.tagNumber.contains(query, ignoreCase = true) }
            .sortedBy { it.name }
}
