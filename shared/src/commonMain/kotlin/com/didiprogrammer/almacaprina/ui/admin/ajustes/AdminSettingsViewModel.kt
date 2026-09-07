package com.didiprogrammer.almacaprina.ui.admin.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.costPerLiter
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.business.totalLitersDay
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.BusinessSettings
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import com.didiprogrammer.almacaprina.security.PinManager
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Bundle de los datos crudos que necesita Ajustes — se piden todos en paralelo, ver [AdminSettingsViewModel.load]. */
private data class AdminSettingsRawData(
    val settings: BusinessSettings?,
    val purchases: List<Purchase>,
    val healthRecords: List<HealthRecord>,
    val feedingRecords: List<FeedingRecord>,
    val milkRecords: List<MilkProductionRecord>
)

/** Sección "Más · Ajustes". Ver CLAUDE.md § Configuración. */
class AdminSettingsViewModel(
    private val businessSettingsRepository: BusinessSettingsRepository,
    private val purchaseRepository: PurchaseRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val feedingRecordRepository: FeedingRecordRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val start = today.minus(29, DateTimeUnit.DAY)

                // Los 5 repositorios son independientes — se piden todos a la vez.
                val raw = coroutineScope {
                    val settingsDeferred = async { businessSettingsRepository.getAll().firstOrNull() }
                    val purchasesDeferred = async { purchaseRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    val feedingRecordsDeferred = async { feedingRecordRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    AdminSettingsRawData(
                        settings = settingsDeferred.await(),
                        purchases = purchasesDeferred.await(),
                        healthRecords = healthRecordsDeferred.await(),
                        feedingRecords = feedingRecordsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await()
                    )
                }

                val purchasesInPeriod = raw.purchases.filter { it.date >= start && it.date <= today }
                val healthInPeriod = raw.healthRecords.filter { it.date >= start && it.date <= today }
                val feedingInPeriod = raw.feedingRecords.filter { it.date >= start && it.date <= today }
                val litersInPeriod = raw.milkRecords
                    .filter { it.date >= start && it.date <= today }
                    .sumOf { it.totalLitersDay() }
                val costPerLiterValue = costPerLiter(purchasesInPeriod, healthInPeriod, feedingInPeriod, litersInPeriod)

                val settings = raw.settings
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        settingsId = settings?.id,
                        farmName = settings?.farmName ?: "",
                        currency = settings?.currency ?: "COP",
                        targetDailyLitersGoalText = settings?.targetDailyLitersGoal?.let(::formatQuantity) ?: "",
                        depositAlertDaysText = settings?.depositAlertDays?.toString() ?: "",
                        costPerLiter = costPerLiterValue,
                        hasPin = AuthService.currentUserId()?.let { PinManager.hasPin(it) } == true
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "No se pudo cargar la configuración") }
            }
        }
    }

    fun onFarmNameChanged(value: String) = _uiState.update { it.copy(farmName = value) }
    fun onCurrencyChanged(value: String) = _uiState.update { it.copy(currency = value) }
    fun onTargetDailyLitersGoalChanged(value: String) = _uiState.update { it.copy(targetDailyLitersGoalText = value) }
    fun onDepositAlertDaysChanged(value: String) = _uiState.update { it.copy(depositAlertDaysText = value) }
    fun onMessageShown() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val settings = BusinessSettings(
                    id = state.settingsId ?: newId(),
                    farmName = state.farmName.trim(),
                    currency = state.currency.trim(),
                    targetDailyLitersGoal = state.targetDailyLitersGoalText.toDouble(),
                    depositAlertDays = state.depositAlertDaysText.toInt(),
                    updatedAt = today
                )
                val saved = if (state.settingsId != null) {
                    businessSettingsRepository.update(state.settingsId, settings)
                } else {
                    businessSettingsRepository.insert(settings)
                }
                _uiState.update {
                    it.copy(isSaving = false, settingsId = saved.id, successMessage = "Configuración guardada")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar la configuración") }
            }
        }
    }

    /** El PIN es solo un candado local — cambiarlo no requiere tocar la sesión de Supabase. */
    fun onSetPin(pin: String) {
        val userId = AuthService.currentUserId() ?: return
        PinManager.setPin(pin, userId)
        _uiState.update { it.copy(hasPin = true, successMessage = "PIN actualizado") }
    }

    fun onRemovePin() {
        PinManager.clearPin()
        _uiState.update { it.copy(hasPin = false, successMessage = "PIN eliminado") }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            AuthService.signOut()
            onLoggedOut()
        }
    }
}
