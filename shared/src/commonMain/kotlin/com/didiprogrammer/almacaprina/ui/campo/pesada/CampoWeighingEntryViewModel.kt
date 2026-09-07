package com.didiprogrammer.almacaprina.ui.campo.pesada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class CampoWeighingEntryViewModel(
    private val goatId: String,
    private val goatRepository: GoatRepository,
    private val weightRecordRepository: WeightRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoWeighingEntryUiState())
    val uiState: StateFlow<CampoWeighingEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val goat = goatRepository.getById(goatId)
                _uiState.update { it.copy(isLoading = false, goat = goat, date = today) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "No se pudo cargar la cabra") }
            }
        }
    }

    fun onDateChanged(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onWeightChanged(value: String) = _uiState.update { it.copy(weightText = value) }
    fun onBcsChanged(value: Int?) = _uiState.update { it.copy(bodyConditionScore = value) }
    fun onNotesChanged(value: String) = _uiState.update { it.copy(notes = value) }

    fun save() {
        val state = _uiState.value
        val weight = state.weight ?: return
        val date = state.date ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                weightRecordRepository.insert(
                    WeightRecord(
                        id = newId(),
                        goatId = goatId,
                        date = date,
                        weightKg = weight,
                        bodyConditionScore = state.bodyConditionScore,
                        notes = state.notes.ifBlank { null }
                    )
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar la pesada") }
            }
        }
    }
}
