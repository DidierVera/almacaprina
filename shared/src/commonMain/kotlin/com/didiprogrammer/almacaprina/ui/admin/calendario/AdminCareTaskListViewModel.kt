package com.didiprogrammer.almacaprina.ui.admin.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sección 6, pantalla 6.1 — Lista de tareas del calendario de cuidado. */
class AdminCareTaskListViewModel(
    private val careTaskRepository: CareTaskRepository,
    private val insumoRepository: InsumoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCareTaskListUiState())
    val uiState: StateFlow<AdminCareTaskListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                // Los 2 repositorios son independientes — se piden ambos a la vez.
                val (tasks, insumosById) = coroutineScope {
                    val tasksDeferred = async { careTaskRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    tasksDeferred.await() to insumosDeferred.await().associateBy { it.id }
                }
                val items = tasks
                    .sortedByDescending { it.active }
                    .map { task -> CareTaskListItem(task = task, insumoName = task.insumoId?.let { insumosById[it]?.name }) }
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, items = items) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: "No se pudo cargar el calendario") }
            }
        }
    }
}
