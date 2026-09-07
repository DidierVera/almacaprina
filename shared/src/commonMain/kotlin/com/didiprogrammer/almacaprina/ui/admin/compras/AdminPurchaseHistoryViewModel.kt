package com.didiprogrammer.almacaprina.ui.admin.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Bundle de los datos crudos del historial — se piden todos en paralelo, ver [AdminPurchaseHistoryViewModel.load]. */
private data class AdminPurchaseHistoryRawData(
    val purchases: List<Purchase>,
    val insumosById: Map<String, Insumo>,
    val packagingsById: Map<String, Packaging>,
    val currency: String
)

/** Sección 5, pantalla 5.1 — Historial de compras. */
class AdminPurchaseHistoryViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val insumoRepository: InsumoRepository,
    private val packagingRepository: PackagingRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPurchaseHistoryUiState())
    val uiState: StateFlow<AdminPurchaseHistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                // Los 4 repositorios son independientes — se piden todos a la vez.
                val (purchases, insumosById, packagingsById, currency) = coroutineScope {
                    val purchasesDeferred = async { purchaseRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    AdminPurchaseHistoryRawData(
                        purchases = purchasesDeferred.await(),
                        insumosById = insumosDeferred.await().associateBy { it.id },
                        packagingsById = packagingsDeferred.await().associateBy { it.id },
                        currency = currencyDeferred.await()
                    )
                }
                val items = purchases.map { purchase ->
                    val name = purchase.insumoId?.let { insumosById[it]?.name }
                        ?: purchase.packagingId?.let { packagingsById[it]?.name }
                        ?: "Sin especificar"
                    PurchaseHistoryItem(purchase = purchase, itemName = name)
                }
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, allItems = items, currency = currency) }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: "No se pudo cargar el historial de compras") }
            }
        }
    }

    fun onCategorySelected(category: PurchaseCategory?) = _uiState.update { it.copy(selectedCategory = category) }
    fun onFromDateChanged(date: LocalDate?) = _uiState.update { it.copy(fromDate = date) }
    fun onToDateChanged(date: LocalDate?) = _uiState.update { it.copy(toDate = date) }
}
