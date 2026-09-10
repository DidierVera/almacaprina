package com.didiprogrammer.almacaprina.ui.campo.alertas

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_alerts_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.dailyHealthReminders
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

class CampoAlertsViewModel(
    private val goatRepository: GoatRepository,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoAlertsUiState())
    val uiState: StateFlow<CampoAlertsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val (goats, healthRecords) = coroutineScope {
                    val goatsDeferred = async { goatRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    goatsDeferred.await() to healthRecordsDeferred.await()
                }
                val goatsById = goats.associateBy { it.id }
                val reminders = dailyHealthReminders(healthRecords, goatsById, today)
                _uiState.update { it.copy(isLoading = false, reminders = reminders) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.campo_alerts_error_load)) }
            }
        }
    }
}
