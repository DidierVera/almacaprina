package com.didiprogrammer.almacaprina.ui.ventas.pendientes

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.pending_list_deleted_customer_fallback
import almacaprina.shared.generated.resources.pending_list_error_load
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.pendingBalancesByCustomer
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

private data class PendingListRawData(
    val sales: List<Sale>,
    val customers: List<Customer>,
    val packagings: List<Packaging>,
    val currency: String
)

/** Ventas · Cobrar pendientes (lista). Ver mockup Ventas-selection-pending.png. */
class PendingSalesListViewModel(
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val packagingRepository: PackagingRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingSalesListUiState())
    val uiState: StateFlow<PendingSalesListUiState> = _uiState.asStateFlow()

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
                val raw = coroutineScope {
                    val salesDeferred = async { saleRepository.getAll() }
                    val customersDeferred = async { customerRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    PendingListRawData(
                        sales = salesDeferred.await(),
                        customers = customersDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val customersById = raw.customers.associateBy { it.id }
                val packagingsById = raw.packagings.associateBy { it.id }
                val balances = pendingBalancesByCustomer(raw.sales, packagingsById)
                val deletedCustomerFallback = getString(Res.string.pending_list_deleted_customer_fallback)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        currency = raw.currency,
                        today = today,
                        totalPending = balances.sumOf { b -> b.totalPending },
                        items = balances.map { balance ->
                            PendingCustomerItem(
                                customerId = balance.customerId,
                                customerName = customersById[balance.customerId]?.name ?: deletedCustomerFallback,
                                totalPending = balance.totalPending,
                                pendingSalesCount = balance.pendingSalesCount,
                                daysSinceOldest = balance.oldestPendingDate.daysUntil(today)
                            )
                        }
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.pending_list_error_load)) }
            }
        }
    }
}
