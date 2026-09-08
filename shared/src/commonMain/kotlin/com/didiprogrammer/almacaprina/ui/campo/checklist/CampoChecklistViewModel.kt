package com.didiprogrammer.almacaprina.ui.campo.checklist

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.campo_checklist_error_confirm_reminder
import almacaprina.shared.generated.resources.campo_checklist_error_confirm_task
import almacaprina.shared.generated.resources.campo_checklist_error_load
import almacaprina.shared.generated.resources.campo_checklist_success_reminder_confirmed
import almacaprina.shared.generated.resources.campo_checklist_success_task_completed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.buildDailyCareChecklist
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.AnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskLog
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.repository.CareTaskLogRepository
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
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
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

private data class ChecklistRawData(
    val goats: List<Goat>,
    val careTasks: List<CareTask>,
    val careTaskLogs: List<CareTaskLog>,
    val healthRecords: List<HealthRecord>,
    val milkRecords: List<MilkProductionRecord>,
    val insumos: List<Insumo>
)

/**
 * Campo · Checklist de hoy. Combina tareas de grupo (CareTask) y recordatorios de salud
 * individuales (HealthRecord) — ver `business/buildDailyCareChecklist`.
 *
 * Al completar una CareTask de tipo `medication` se genera automáticamente un HealthRecord de
 * GRUPO (`goat_id = null`, ver docs/data_model.md § 5. HealthRecord), igual que `feeding` genera
 * un FeedingRecord de grupo. `type` se guarda como `TREATMENT` por defecto, ya que `CareTask` no
 * tiene un sub-tipo propio para distinguir vacuna/desparasitación/tratamiento.
 */
class CampoChecklistViewModel(
    private val goatRepository: GoatRepository,
    private val careTaskRepository: CareTaskRepository,
    private val careTaskLogRepository: CareTaskLogRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val feedingRecordRepository: FeedingRecordRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val insumoRepository: InsumoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoChecklistUiState())
    val uiState: StateFlow<CampoChecklistUiState> = _uiState.asStateFlow()

    private var today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)
    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val raw = coroutineScope {
                    val goatsDeferred = async { goatRepository.getAll() }
                    val careTasksDeferred = async { careTaskRepository.getAll() }
                    val careTaskLogsDeferred = async { careTaskLogRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    ChecklistRawData(
                        goats = goatsDeferred.await(),
                        careTasks = careTasksDeferred.await(),
                        careTaskLogs = careTaskLogsDeferred.await(),
                        healthRecords = healthRecordsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await(),
                        insumos = insumosDeferred.await()
                    )
                }
                val goatsById = raw.goats.associateBy { it.id }
                val activeGoats = raw.goats.filter { it.exitDate == null }
                val todayMilkRecords = raw.milkRecords.filter { it.date == today }

                val checklist = buildDailyCareChecklist(
                    activeCareTasks = raw.careTasks,
                    careTaskLogs = raw.careTaskLogs,
                    healthRecords = raw.healthRecords,
                    goatsById = goatsById,
                    activeGoats = activeGoats,
                    todayMilkRecords = todayMilkRecords,
                    today = today
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        checklist = checklist,
                        insumosById = raw.insumos.associateBy { insumo -> insumo.id }
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.campo_checklist_error_load)) }
            }
        }
    }

    fun onCareTaskClicked(task: CareTask) {
        _uiState.update {
            it.copy(confirmingCareTask = task, confirmQuantityText = task.quantityPerOccurrence?.toString() ?: "")
        }
    }

    fun onDismissCareTaskConfirm() = _uiState.update { it.copy(confirmingCareTask = null, confirmQuantityText = "") }
    fun onConfirmQuantityChanged(value: String) = _uiState.update { it.copy(confirmQuantityText = value) }

    fun confirmCareTask() {
        val task = _uiState.value.confirmingCareTask ?: return
        val quantity = _uiState.value.confirmQuantityText.toDoubleOrNull()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                var linkedRecordId: String? = null
                if (task.taskType == CareTaskType.FEEDING && task.insumoId != null && quantity != null) {
                    val record = feedingRecordRepository.insert(
                        FeedingRecord(
                            id = newId(),
                            date = today,
                            animalGroup = task.animalGroup?.toFeedingAnimalGroupOrNull(),
                            goatId = null,
                            insumoId = task.insumoId,
                            quantity = quantity,
                            cost = null
                        )
                    )
                    linkedRecordId = record.id
                } else if (task.taskType == CareTaskType.MEDICATION && task.insumoId != null) {
                    val record = healthRecordRepository.insert(
                        HealthRecord(
                            id = newId(),
                            goatId = null,
                            type = HealthRecordType.TREATMENT,
                            date = today,
                            description = task.name,
                            insumoId = task.insumoId,
                            dosage = null,
                            quantityUsed = quantity,
                            milkWithdrawalDays = null,
                            cost = null,
                            veterinarian = null,
                            nextSuggestedDate = null
                        )
                    )
                    linkedRecordId = record.id
                }

                careTaskLogRepository.insert(
                    CareTaskLog(
                        id = newId(),
                        careTaskId = task.id,
                        date = today,
                        completed = true,
                        actualQuantityUsed = quantity,
                        linkedRecordId = linkedRecordId
                    )
                )
                _uiState.update { it.copy(isSaving = false, confirmingCareTask = null, confirmQuantityText = "", successMessage = getString(Res.string.campo_checklist_success_task_completed)) }
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.campo_checklist_error_confirm_task)) }
            }
        }
    }

    fun onHealthReminderClicked(healthRecord: HealthRecord) {
        _uiState.update {
            it.copy(confirmingHealthReminder = healthRecord, confirmHealthQuantityText = healthRecord.quantityUsed?.toString() ?: "")
        }
    }

    fun onDismissHealthReminderConfirm() = _uiState.update { it.copy(confirmingHealthReminder = null, confirmHealthQuantityText = "") }
    fun onConfirmHealthQuantityChanged(value: String) = _uiState.update { it.copy(confirmHealthQuantityText = value) }

    fun confirmHealthReminder() {
        val original = _uiState.value.confirmingHealthReminder ?: return
        val quantity = _uiState.value.confirmHealthQuantityText.toDoubleOrNull()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                // Se crea un HealthRecord NUEVO (no se edita el original) — ver docs/data_model.md § 5.
                healthRecordRepository.insert(
                    HealthRecord(
                        id = newId(),
                        goatId = original.goatId,
                        type = original.type,
                        date = today,
                        description = original.description,
                        insumoId = original.insumoId,
                        dosage = original.dosage,
                        quantityUsed = quantity,
                        milkWithdrawalDays = original.milkWithdrawalDays,
                        cost = null,
                        veterinarian = original.veterinarian,
                        nextSuggestedDate = null
                    )
                )
                _uiState.update { it.copy(isSaving = false, confirmingHealthReminder = null, confirmHealthQuantityText = "", successMessage = getString(Res.string.campo_checklist_success_reminder_confirmed)) }
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.campo_checklist_error_confirm_reminder)) }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            AuthService.signOut()
            onLoggedOut()
        }
    }
}

private fun CareTaskAnimalGroup.toFeedingAnimalGroupOrNull(): AnimalGroup = when (this) {
    CareTaskAnimalGroup.LACTATING -> AnimalGroup.LACTATING
    CareTaskAnimalGroup.PREGNANT -> AnimalGroup.PREGNANT
    CareTaskAnimalGroup.YOUNG_DOES -> AnimalGroup.YOUNG_DOES
    CareTaskAnimalGroup.DRY -> AnimalGroup.DRY
    CareTaskAnimalGroup.BREEDING_BUCKS -> AnimalGroup.BREEDING_BUCKS
    CareTaskAnimalGroup.GENERAL, CareTaskAnimalGroup.ALL -> AnimalGroup.GENERAL
}
