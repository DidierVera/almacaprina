package com.didiprogrammer.almacaprina.ui.ventas.envases

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.deposits_detail_error_load
import almacaprina.shared.generated.resources.deposits_detail_error_save
import almacaprina.shared.generated.resources.deposits_detail_success_returned
import almacaprina.shared.generated.resources.deposits_detail_unknown_customer_fallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.packagingUnitsOutByCustomer
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.DepositMovementType
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.util.newId
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

private data class DepositsDetailRawData(
    val customer: Customer?,
    val packagings: List<Packaging>,
    val transactions: List<PackagingDepositTransaction>,
    val currency: String
)

/** Ventas · Devolver envase (detalle por cliente). */
class VentasDepositsDetailViewModel(
    private val customerId: String,
    private val customerRepository: CustomerRepository,
    private val packagingRepository: PackagingRepository,
    private val packagingDepositTransactionRepository: PackagingDepositTransactionRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentasDepositsDetailUiState())
    val uiState: StateFlow<VentasDepositsDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val raw = coroutineScope {
                    val customerDeferred = async { customerRepository.getById(customerId) }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val transactionsDeferred = async { packagingDepositTransactionRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    DepositsDetailRawData(
                        customer = customerDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        transactions = transactionsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val packagingsById = raw.packagings.associateBy { it.id }
                val rows = packagingUnitsOutByCustomer(raw.transactions)
                    .filter { it.customerId == customerId }
                    .mapNotNull { row ->
                        val packaging = packagingsById[row.packagingId] ?: return@mapNotNull null
                        DepositPackagingRow(
                            packagingId = row.packagingId,
                            packagingName = packaging.name,
                            unitsOut = row.unitsOut,
                            depositAmountPerUnit = packaging.depositAmount ?: 0.0,
                            returnQuantityText = row.unitsOut.toString()
                        )
                    }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currency = raw.currency,
                        customerName = raw.customer?.name ?: getString(Res.string.deposits_detail_unknown_customer_fallback),
                        rows = rows
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.deposits_detail_error_load)) }
            }
        }
    }

    fun onReturnQuantityChanged(packagingId: String, value: String) = _uiState.update { state ->
        state.copy(rows = state.rows.map { if (it.packagingId == packagingId) it.copy(returnQuantityText = value) else it })
    }

    fun confirmReturn(packagingId: String) {
        val row = _uiState.value.rows.firstOrNull { it.packagingId == packagingId } ?: return
        val quantity = row.returnQuantity ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                packagingDepositTransactionRepository.insert(
                    PackagingDepositTransaction(
                        id = newId(),
                        customerId = customerId,
                        saleId = null,
                        packagingId = packagingId,
                        date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        movementType = DepositMovementType.DEPOSIT_RETURNED,
                        quantity = quantity
                    )
                )
                _uiState.update { it.copy(isSaving = false, successMessage = getString(Res.string.deposits_detail_success_returned)) }
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.deposits_detail_error_save)) }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
