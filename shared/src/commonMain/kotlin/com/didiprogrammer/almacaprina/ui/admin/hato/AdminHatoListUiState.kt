package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_hato_sort_age
import almacaprina.shared.generated.resources.admin_hato_sort_name
import almacaprina.shared.generated.resources.admin_hato_sort_tag_number
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import org.jetbrains.compose.resources.stringResource

data class GoatListItem(
    val goat: Goat,
    val contextualInfo: String
)

/** Criterio de orden de la lista de hato — ver [AdminHatoListViewModel.applyFilters]. */
enum class GoatSortOption {
    NAME,
    TAG_NUMBER,
    AGE
}

/** Etiqueta en español de un GoatSortOption — solo se puede llamar desde contexto @Composable. */
@Composable
fun GoatSortOption.label(): String = stringResource(
    when (this) {
        GoatSortOption.NAME -> Res.string.admin_hato_sort_name
        GoatSortOption.TAG_NUMBER -> Res.string.admin_hato_sort_tag_number
        GoatSortOption.AGE -> Res.string.admin_hato_sort_age
    }
)

data class AdminHatoListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedStatus: GoatStatus? = null,
    val sortOption: GoatSortOption = GoatSortOption.NAME,
    val items: List<GoatListItem> = emptyList()
)
