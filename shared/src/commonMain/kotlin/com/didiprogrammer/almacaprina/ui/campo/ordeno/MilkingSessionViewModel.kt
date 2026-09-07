package com.didiprogrammer.almacaprina.ui.campo.ordeno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.lactationNumber
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.NoMilkingReason
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private data class MilkingRawData(
    val goats: List<Goat>,
    val milkRecords: List<MilkProductionRecord>,
    val reproductiveEvents: List<ReproductiveEvent>
)

/**
 * ViewModel único para las 3 pantallas de la sesión de ordeño (`campo/ordeno`, `.../registro/{goatId}`,
 * `.../resumen`) — se resuelve con el `viewModelStoreOwner` del grafo anidado, igual que
 * NewSaleViewModel en Ventas, para que las 3 pantallas compartan una sola instancia.
 */
class MilkingSessionViewModel(
    private val goatRepository: GoatRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val reproductiveEventRepository: ReproductiveEventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MilkingSessionUiState())
    val uiState: StateFlow<MilkingSessionUiState> = _uiState.asStateFlow()

    private var today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private var existingRecordsByGoat: Map<String, MilkProductionRecord> = emptyMap()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
                val isEvening = hour >= 12

                val raw = coroutineScope {
                    val goatsDeferred = async { goatRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val reproEventsDeferred = async { reproductiveEventRepository.getAll() }
                    MilkingRawData(goatsDeferred.await(), milkRecordsDeferred.await(), reproEventsDeferred.await())
                }

                val eligibleGoats = raw.goats
                    .filter { it.exitDate == null && it.currentStatus == GoatStatus.IN_PRODUCTION }
                    .sortedBy { it.name }
                val todayRecordsByGoat = raw.milkRecords.filter { it.date == today }.associateBy { it.goatId }
                existingRecordsByGoat = todayRecordsByGoat

                val entries = eligibleGoats.map { goat ->
                    val record = todayRecordsByGoat[goat.id]
                    val sessionValue = record?.let { if (isEvening) it.eveningMilkingLiters else it.morningMilkingLiters }
                    MilkingGoatEntry(
                        goat = goat,
                        lactationNumber = lactationNumber(goat.id, raw.reproductiveEvents),
                        sessionLiters = sessionValue,
                        noMilkingReason = record?.noMilkingReason
                    )
                }

                _uiState.update { it.copy(isLoading = false, isEveningSession = isEvening, entries = entries) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "No se pudo cargar la sesión de ordeño") }
            }
        }
    }

    fun onSelectGoat(goatId: String) {
        val entry = _uiState.value.entries.firstOrNull { it.goat.id == goatId }
        _uiState.update {
            it.copy(
                selectedGoatId = goatId,
                currentLitersText = entry?.sessionLiters?.toString() ?: "0"
            )
        }
    }

    fun onClearSelection() = _uiState.update { it.copy(selectedGoatId = null, currentLitersText = "0", showReasonPicker = false) }

    fun onLitersChanged(text: String) = _uiState.update { it.copy(currentLitersText = text) }
    fun onToggleKeypad(useKeypad: Boolean) = _uiState.update { it.copy(useNumericKeypad = useKeypad) }
    fun onShowReasonPicker(show: Boolean) = _uiState.update { it.copy(showReasonPicker = show) }

    fun saveCurrentEntry(onSaved: () -> Unit) {
        val state = _uiState.value
        val goatId = state.selectedGoatId ?: return
        val liters = state.currentLitersValue
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                upsertMilkRecord(goatId, sessionLiters = liters, reason = null)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        entries = state.entries.map { entry ->
                            if (entry.goat.id == goatId) entry.copy(sessionLiters = liters, noMilkingReason = null) else entry
                        }
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar el ordeño") }
            }
        }
    }

    fun markUnmilked(reason: NoMilkingReason, onSaved: () -> Unit) {
        val goatId = _uiState.value.selectedGoatId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, showReasonPicker = false) }
            try {
                upsertMilkRecord(goatId, sessionLiters = null, reason = reason)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        entries = state.entries.map { entry ->
                            if (entry.goat.id == goatId) entry.copy(sessionLiters = null, noMilkingReason = reason) else entry
                        }
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar el ordeño") }
            }
        }
    }

    private suspend fun upsertMilkRecord(goatId: String, sessionLiters: Double?, reason: NoMilkingReason?) {
        val isEvening = _uiState.value.isEveningSession
        val existing = existingRecordsByGoat[goatId]
        val updated = if (existing != null) {
            // no_milking_reason es un solo campo para todo el día (no por sesión) — si esta
            // sesión sí registra litros, se conserva el motivo que ya hubiera de la otra sesión.
            if (isEvening) {
                existing.copy(eveningMilkingLiters = sessionLiters, noMilkingReason = reason ?: existing.noMilkingReason)
            } else {
                existing.copy(morningMilkingLiters = sessionLiters, noMilkingReason = reason ?: existing.noMilkingReason)
            }
        } else {
            MilkProductionRecord(
                id = newId(),
                goatId = goatId,
                date = today,
                morningMilkingLiters = if (!isEvening) sessionLiters else null,
                eveningMilkingLiters = if (isEvening) sessionLiters else null,
                noMilkingReason = reason
            )
        }
        val saved = if (existing != null) {
            milkProductionRecordRepository.update(existing.id, updated)
        } else {
            milkProductionRecordRepository.insert(updated)
        }
        existingRecordsByGoat = existingRecordsByGoat + (goatId to saved)
    }
}
