package com.didiprogrammer.almacaprina.ui.ventas.envases

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.deposits_list_error_load
import almacaprina.shared.generated.resources.pending_list_deleted_customer_fallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.packagingUnitsOutByCustomer
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

private data class DepositsListRawData(
    val customers: List<Customer>,
    val packagings: List<Packaging>,
    val transactions: List<PackagingDepositTransaction>,
    val currency: String
)

/** Ventas · Devolver envase (lista). Ver business/packagingUnitsOutByCustomer. */
class VentasDepositsListViewModel(
    private val customerRepository: CustomerRepository,
    private val packagingRepository: PackagingRepository,
    private val packagingDepositTransactionRepository: PackagingDepositTransactionRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentasDepositsListUiState())
    val uiState: StateFlow<VentasDepositsListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)
    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                val raw = coroutineScope {
                    val customersDeferred = async { customerRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val transactionsDeferred = async { packagingDepositTransactionRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    DepositsListRawData(
                        customers = customersDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        transactions = transactionsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val customersById = raw.customers.associateBy { it.id }
                val packagingsById = raw.packagings.associateBy { it.id }
                val deletedCustomerFallback = getString(Res.string.pending_list_deleted_customer_fallback)

                val items = packagingUnitsOutByCustomer(raw.transactions)
                    .groupBy { it.customerId }
                    .map { (customerId, rows) ->
                        DepositCustomerItem(
                            customerId = customerId,
                            customerName = customersById[customerId]?.name ?: deletedCustomerFallback,
                            totalUnitsOut = rows.sumOf { it.unitsOut },
                            totalDepositValue = rows.sumOf { it.unitsOut * (packagingsById[it.packagingId]?.depositAmount ?: 0.0) }
                        )
                    }
                    .sortedByDescending { it.totalUnitsOut }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        currency = raw.currency,
                        totalUnitsOut = items.sumOf { item -> item.totalUnitsOut },
                        totalDepositValue = items.sumOf { item -> item.totalDepositValue },
                        items = items
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.deposits_list_error_load)) }
            }
        }
    }
}
