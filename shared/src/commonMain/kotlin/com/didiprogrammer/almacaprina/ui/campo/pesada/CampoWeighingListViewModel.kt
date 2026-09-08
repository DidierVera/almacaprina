package com.didiprogrammer.almacaprina.ui.campo.pesada

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.weighing_list_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.overdueWeighings
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
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

/** Campo · Pesada — lista de cabras con pesada vencida. Reutiliza `overdueWeighings` (ya existía). */
class CampoWeighingListViewModel(
    private val goatRepository: GoatRepository,
    private val weightRecordRepository: WeightRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampoWeighingListUiState())
    val uiState: StateFlow<CampoWeighingListUiState> = _uiState.asStateFlow()

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
                val (goats, weightRecords) = coroutineScope {
                    val goatsDeferred = async { goatRepository.getAll() }
                    val weightRecordsDeferred = async { weightRecordRepository.getAll() }
                    goatsDeferred.await() to weightRecordsDeferred.await()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        overdueGoats = overdueWeighings(goats, weightRecords, today)
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.weighing_list_error_load)) }
            }
        }
    }
}
