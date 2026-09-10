package com.didiprogrammer.almacaprina.ui.campo.cabras

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_goat_health_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class CampoGoatHealthDetailViewModel(
    private val goatId: String,
    private val goatRepository: GoatRepository,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoGoatHealthDetailUiState())
    val uiState: StateFlow<CampoGoatHealthDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (goat, healthRecordsRaw) = coroutineScope {
                    val goatDeferred = async { goatRepository.getById(goatId) }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    goatDeferred.await() to healthRecordsDeferred.await()
                }
                val healthRecords = healthRecordsRaw
                    .filter { it.goatId == goatId }
                    .sortedByDescending { it.date }
                _uiState.update { it.copy(isLoading = false, goat = goat, healthRecords = healthRecords) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.campo_goat_health_error_load)) }
            }
        }
    }
}
