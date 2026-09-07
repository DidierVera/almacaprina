package com.didiprogrammer.almacaprina.ui.admin.calendario

import com.didiprogrammer.almacaprina.domain.model.CareTask

data class CareTaskListItem(
    val task: CareTask,
    val insumoName: String?
)

data class AdminCareTaskListUiState(
    val isLoading: Boolean = true,
    val items: List<CareTaskListItem> = emptyList()
)
