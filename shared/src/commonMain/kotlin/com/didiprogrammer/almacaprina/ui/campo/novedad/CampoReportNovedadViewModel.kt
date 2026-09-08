package com.didiprogrammer.almacaprina.ui.campo.novedad

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_novedad_error_load
import almacaprina.shared.generated.resources.campo_novedad_error_save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
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
import org.jetbrains.compose.resources.getString

class CampoReportNovedadViewModel(
    private val goatRepository: GoatRepository,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoReportNovedadUiState())
    val uiState: StateFlow<CampoReportNovedadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val goats = goatRepository.getAll()
                _uiState.update { it.copy(isLoading = false, allGoats = goats, date = today) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.campo_novedad_error_load)) }
            }
        }
    }

    fun onGoatSelected(goat: Goat) = _uiState.update { it.copy(selectedGoat = goat) }
    fun onDateChanged(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onTypeChanged(value: HealthRecordType) = _uiState.update { it.copy(type = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(description = value) }
    fun onNextSuggestedDateChanged(value: LocalDate?) = _uiState.update { it.copy(nextSuggestedDate = value) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        val goat = state.selectedGoat ?: return
        val date = state.date ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                healthRecordRepository.insert(
                    HealthRecord(
                        id = newId(),
                        goatId = goat.id,
                        type = state.type,
                        date = date,
                        description = state.description.trim(),
                        nextSuggestedDate = state.nextSuggestedDate
                    )
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.campo_novedad_error_save)) }
            }
        }
    }
}
