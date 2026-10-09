package com.didiprogrammer.almacaprina.ui.campo.ordeno

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.milking_session_error_load
import almacaprina.shared.generated.resources.milking_session_error_save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.MilkEntryUnit
import com.didiprogrammer.almacaprina.business.fromMilliliters
import com.didiprogrammer.almacaprina.business.lactationNumber
import com.didiprogrammer.almacaprina.business.roundTo1Decimal
import com.didiprogrammer.almacaprina.business.toMilliliters
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
import kotlin.math.round
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

private data class MilkingRawData(
    val goats: List<Goat>,
    val milkRecords: List<MilkProductionRecord>,
    val reproductiveEvents: List<ReproductiveEvent>
)

/** Valor precargado al editar un registro ya guardado: texto mostrado, unidad y los ml originales. */
private data class Prefill(val text: String, val unit: MilkEntryUnit, val ml: Double)

private fun formatEntryValue(value: Double, unit: MilkEntryUnit): String {
    val rounded = if (unit == MilkEntryUnit.MILLILITER) round(value) else roundTo1Decimal(value)
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString().replace('.', ',')
}

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
    private var prefill: Prefill? = null

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
                    val sessionValue = record?.let { if (isEvening) it.eveningMilkingMl else it.morningMilkingMl }
                    MilkingGoatEntry(
                        goat = goat,
                        lactationNumber = lactationNumber(goat.id, raw.reproductiveEvents),
                        sessionMl = sessionValue,
                        noMilkingReason = record?.noMilkingReason
                    )
                }

                _uiState.update { it.copy(isLoading = false, isEveningSession = isEvening, entries = entries) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.milking_session_error_load)) }
            }
        }
    }

    fun onSelectGoat(goatId: String) {
        val entry = _uiState.value.entries.firstOrNull { it.goat.id == goatId }
        val unit = _uiState.value.entryUnit
        val savedMl = entry?.sessionMl
        val text = if (savedMl != null) formatEntryValue(unit.fromMilliliters(savedMl), unit) else "0"
        prefill = savedMl?.let { Prefill(text, unit, it) }
        _uiState.update { it.copy(selectedGoatId = goatId, currentValueText = text) }
    }

    fun onClearSelection() {
        prefill = null
        _uiState.update { it.copy(selectedGoatId = null, currentValueText = "0", showReasonPicker = false) }
    }

    fun onValueChanged(text: String) = _uiState.update { it.copy(currentValueText = text) }

    /** Cambia ml ↔ oz conservando la cantidad ya digitada (se reexpresa en la nueva unidad). */
    fun onUnitChanged(unit: MilkEntryUnit) {
        val state = _uiState.value
        if (unit == state.entryUnit) return
        val untouched = prefill?.let { it.unit == state.entryUnit && it.text == state.currentValueText } == true
        val ml = resolveMl(state)
        val text = if (ml > 0) formatEntryValue(unit.fromMilliliters(ml), unit) else "0"
        prefill = if (untouched) Prefill(text, unit, ml) else null
        _uiState.update { it.copy(entryUnit = unit, currentValueText = text) }
    }

    /** direction = +1 / -1; el paso depende de la unidad (ver [MilkingSessionUiState.stepSize]). */
    fun onStep(direction: Int) = _uiState.update {
        val newValue = (it.currentValue + direction * it.stepSize).coerceAtLeast(0.0)
        it.copy(currentValueText = formatEntryValue(newValue, it.entryUnit))
    }

    fun onToggleKeypad(useKeypad: Boolean) = _uiState.update { it.copy(useNumericKeypad = useKeypad) }
    fun onShowReasonPicker(show: Boolean) = _uiState.update { it.copy(showReasonPicker = show) }

    /**
     * Lo guardado siempre son ml. Si el usuario no tocó un valor precargado se conservan los ml
     * originales (convertir ml → oz → ml puede redondear y alterar un dato que nadie editó).
     */
    private fun resolveMl(state: MilkingSessionUiState): Double {
        val p = prefill
        if (p != null && p.unit == state.entryUnit && p.text == state.currentValueText) return p.ml
        return round(state.entryUnit.toMilliliters(state.currentValue))
    }

    /** ml que se guardarían ahora mismo — para la vista previa cuando se captura en oz. */
    fun currentMl(): Double = resolveMl(_uiState.value)

    fun saveCurrentEntry(onSaved: () -> Unit) {
        val state = _uiState.value
        val goatId = state.selectedGoatId ?: return
        val ml = resolveMl(state)
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                upsertMilkRecord(goatId, sessionMl = ml, reason = null)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        entries = state.entries.map { entry ->
                            if (entry.goat.id == goatId) entry.copy(sessionMl = ml, noMilkingReason = null) else entry
                        }
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.milking_session_error_save)) }
            }
        }
    }

    fun markUnmilked(reason: NoMilkingReason, onSaved: () -> Unit) {
        val goatId = _uiState.value.selectedGoatId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, showReasonPicker = false) }
            try {
                upsertMilkRecord(goatId, sessionMl = null, reason = reason)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        entries = state.entries.map { entry ->
                            if (entry.goat.id == goatId) entry.copy(sessionMl = null, noMilkingReason = reason) else entry
                        }
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.milking_session_error_save)) }
            }
        }
    }

    private suspend fun upsertMilkRecord(goatId: String, sessionMl: Double?, reason: NoMilkingReason?) {
        val isEvening = _uiState.value.isEveningSession
        val existing = existingRecordsByGoat[goatId]
        val updated = if (existing != null) {
            // no_milking_reason es un solo campo para todo el día (no por sesión) — si esta
            // sesión sí registra leche, se conserva el motivo que ya hubiera de la otra sesión.
            if (isEvening) {
                existing.copy(eveningMilkingMl = sessionMl, noMilkingReason = reason ?: existing.noMilkingReason)
            } else {
                existing.copy(morningMilkingMl = sessionMl, noMilkingReason = reason ?: existing.noMilkingReason)
            }
        } else {
            MilkProductionRecord(
                id = newId(),
                goatId = goatId,
                date = today,
                morningMilkingMl = if (!isEvening) sessionMl else null,
                eveningMilkingMl = if (isEvening) sessionMl else null,
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
