package com.didiprogrammer.almacaprina.ui.admin.hato

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus

data class GoatListItem(
    val goat: Goat,
    val contextualInfo: String
)

data class AdminHatoListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedStatus: GoatStatus? = null,
    val items: List<GoatListItem> = emptyList()
)
