package com.didiprogrammer.almacaprina.ui.campo.cabras

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_goats_list_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class CampoGoatListViewModel(
    private val goatRepository: GoatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoGoatListUiState())
    val uiState: StateFlow<CampoGoatListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetch(isRefresh = false)
    fun refresh() = fetch(isRefresh = true)

    private fun fetch(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                val goats = goatRepository.getAll()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, allGoats = goats) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.campo_goats_list_error_load)) }
            }
        }
    }

    fun onQueryChanged(value: String) = _uiState.update { it.copy(query = value) }
}
