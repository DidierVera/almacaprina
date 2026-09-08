package com.didiprogrammer.almacaprina.ui.admin.hato

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_hato_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.expectedBirthDateOrNull
import com.didiprogrammer.almacaprina.business.goatContextualInfo
import com.didiprogrammer.almacaprina.business.totalLitersDay
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventResult
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEventType
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
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

/** Bundle de los datos crudos de la lista — se piden todos en paralelo, ver [AdminHatoListViewModel.load]. */
private data class AdminHatoListRawData(
    val goats: List<Goat>,
    val milkRecords: List<MilkProductionRecord>,
    val reproductiveEvents: List<ReproductiveEvent>,
    val weightRecords: List<WeightRecord>
)

class AdminHatoListViewModel(
    initialStatusFilter: GoatStatus?,
    private val goatRepository: GoatRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val reproductiveEventRepository: ReproductiveEventRepository,
    private val weightRecordRepository: WeightRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHatoListUiState(selectedStatus = initialStatusFilter))
    val uiState: StateFlow<AdminHatoListUiState> = _uiState.asStateFlow()

    private var allGoats: List<Goat> = emptyList()
    private var milkToday: Map<String, Double> = emptyMap()
    private var pendingBirthByDoe: Map<String, LocalDate> = emptyMap()
    private var lastWeightByGoat: Map<String, LocalDate> = emptyMap()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                // Las 4 consultas son independientes — se piden todas a la vez.
                val (goats, milkRecords, reproductiveEvents, weightRecords) = coroutineScope {
                    val goatsDeferred = async { goatRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val reproductiveEventsDeferred = async { reproductiveEventRepository.getAll() }
                    val weightRecordsDeferred = async { weightRecordRepository.getAll() }
                    AdminHatoListRawData(
                        goats = goatsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await(),
                        reproductiveEvents = reproductiveEventsDeferred.await(),
                        weightRecords = weightRecordsDeferred.await()
                    )
                }
                allGoats = goats
                milkToday = milkRecords
                    .filter { it.date == today }
                    .groupBy { it.goatId }
                    .mapValues { (_, records) -> records.sumOf { it.totalLitersDay() } }

                pendingBirthByDoe = reproductiveEvents
                    .filter { it.eventType == ReproductiveEventType.BREEDING && it.result == ReproductiveEventResult.PENDING }
                    .mapNotNull { event -> expectedBirthDateOrNull(event.eventType, event.date)?.let { event.doeId to it } }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { (_, dates) -> dates.min() }

                lastWeightByGoat = weightRecords
                    .groupBy { it.goatId }
                    .mapValues { (_, records) -> records.maxOf { it.date } }

                applyFilters(today)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.admin_hato_error_load)) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch { applyFilters(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    }

    fun onStatusFilterSelected(status: GoatStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        viewModelScope.launch { applyFilters(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    }

    private suspend fun applyFilters(today: LocalDate) {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val status = _uiState.value.selectedStatus

        val filtered = allGoats
            .filter { goat ->
                when {
                    status != null -> goat.currentStatus == status
                    else -> goat.exitDate == null
                }
            }
            .filter { goat ->
                query.isBlank() ||
                    goat.name.lowercase().contains(query) ||
                    goat.tagNumber.lowercase().contains(query)
            }
            .sortedBy { it.name }
            .map { goat ->
                GoatListItem(
                    goat = goat,
                    contextualInfo = goatContextualInfo(
                        goat = goat,
                        today = today,
                        milkLitersToday = milkToday[goat.id],
                        nextExpectedBirth = pendingBirthByDoe[goat.id],
                        lastWeightDate = lastWeightByGoat[goat.id]
                    )
                )
            }

        _uiState.update { it.copy(isLoading = false, isRefreshing = false, items = filtered) }
    }
}
