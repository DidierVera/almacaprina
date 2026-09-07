package com.didiprogrammer.almacaprina.ui.admin.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sección 6, pantalla 6.2 — Nueva/editar tarea. `taskId` nulo = alta. */
class AdminCareTaskFormViewModel(
    private val taskId: String?,
    private val careTaskRepository: CareTaskRepository,
    private val insumoRepository: InsumoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCareTaskFormUiState(isEditing = taskId != null))
    val uiState: StateFlow<AdminCareTaskFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Las 2 consultas son independientes — se piden ambas a la vez.
            val (insumos, existing) = coroutineScope {
                val insumosDeferred = async { insumoRepository.getAll() }
                val existingDeferred = async { taskId?.let { careTaskRepository.getById(it) } }
                insumosDeferred.await() to existingDeferred.await()
            }
            _uiState.update {
                if (existing != null) {
                    it.copy(
                        isLoading = false,
                        insumos = insumos,
                        name = existing.name,
                        taskType = existing.taskType,
                        frequency = existing.frequency,
                        animalGroup = existing.animalGroup,
                        insumoId = existing.insumoId,
                        quantityPerOccurrenceText = existing.quantityPerOccurrence?.toString() ?: "",
                        timeOfDay = existing.timeOfDay,
                        active = existing.active
                    )
                } else {
                    it.copy(isLoading = false, insumos = insumos)
                }
            }
        }
    }

    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value) }
    fun onTaskTypeChanged(value: CareTaskType) = _uiState.update {
        it.copy(taskType = value, insumoId = if (value == CareTaskType.FEEDING || value == CareTaskType.MEDICATION) it.insumoId else null)
    }
    fun onFrequencyChanged(value: CareTaskFrequency) = _uiState.update { it.copy(frequency = value) }
    fun onAnimalGroupChanged(value: CareTaskAnimalGroup?) = _uiState.update { it.copy(animalGroup = value) }
    fun onInsumoSelected(value: String?) = _uiState.update { it.copy(insumoId = value) }
    fun onQuantityChanged(value: String) = _uiState.update { it.copy(quantityPerOccurrenceText = value) }
    fun onTimeOfDayChanged(value: TimeOfDay?) = _uiState.update { it.copy(timeOfDay = value) }
    fun onActiveChanged(value: Boolean) = _uiState.update { it.copy(active = value) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val task = CareTask(
                    id = taskId ?: newId(),
                    name = state.name.trim(),
                    taskType = state.taskType,
                    frequency = state.frequency,
                    animalGroup = state.animalGroup,
                    insumoId = if (state.needsInsumo) state.insumoId else null,
                    quantityPerOccurrence = if (state.needsInsumo) state.quantityPerOccurrence else null,
                    timeOfDay = state.timeOfDay,
                    active = state.active
                )
                if (taskId != null) {
                    careTaskRepository.update(taskId, task)
                } else {
                    careTaskRepository.insert(task)
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar la tarea") }
            }
        }
    }
}
