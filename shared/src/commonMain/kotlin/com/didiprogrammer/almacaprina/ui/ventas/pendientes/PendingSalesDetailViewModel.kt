package com.didiprogrammer.almacaprina.ui.ventas.pendientes

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.pending_detail_error_load
import almacaprina.shared.generated.resources.pending_detail_error_mark_all_paid
import almacaprina.shared.generated.resources.pending_detail_error_mark_paid
import almacaprina.shared.generated.resources.pending_detail_item_deposit_word
import almacaprina.shared.generated.resources.pending_detail_unknown_customer_fallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.formatQuantity
import com.didiprogrammer.almacaprina.business.totalValue
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
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

private data class PendingDetailRawData(
    val customer: Customer?,
    val sales: List<Sale>,
    val products: List<Product>,
    val packagings: List<Packaging>,
    val currency: String
)

/** Ventas · Cobrar pendientes (detalle por cliente). Ver mockup Ventas-selection-details.png. */
class PendingSalesDetailViewModel(
    private val customerId: String,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val packagingRepository: PackagingRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingSalesDetailUiState())
    val uiState: StateFlow<PendingSalesDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val raw = coroutineScope {
                    val customerDeferred = async { customerRepository.getById(customerId) }
                    val salesDeferred = async { saleRepository.getAll() }
                    val productsDeferred = async { productRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    PendingDetailRawData(
                        customer = customerDeferred.await(),
                        sales = salesDeferred.await(),
                        products = productsDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val productsById = raw.products.associateBy { it.id }
                val packagingsById = raw.packagings.associateBy { it.id }
                val pendingSales = raw.sales
                    .filter { it.customerId == customerId && it.paymentStatus == PaymentStatus.PENDING }
                    .sortedByDescending { it.date }

                val items = pendingSales.map { sale ->
                    val product = productsById[sale.productId]
                    val packaging = sale.packagingId?.let { packagingsById[it] }
                    val depositSuffix = if ((sale.newPackagingUnitsCount ?: 0) > 0) {
                        " + " + getString(Res.string.pending_detail_item_deposit_word, sale.newPackagingUnitsCount ?: 0)
                    } else {
                        ""
                    }
                    val packagingSuffix = packaging?.let { " · ${it.name.lowercase()}" } ?: ""
                    PendingSaleItem(
                        saleId = sale.id,
                        date = sale.date,
                        description = "${formatQuantity(sale.quantitySold)} ${product?.saleUnit?.name?.take(1) ?: ""}$packagingSuffix$depositSuffix",
                        amount = sale.totalValue(packaging)
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currency = raw.currency,
                        customerName = raw.customer?.name ?: getString(Res.string.pending_detail_unknown_customer_fallback),
                        totalPending = items.sumOf { item -> item.amount },
                        daysSinceOldest = pendingSales.minOfOrNull { it.date }?.daysUntil(today) ?: 0,
                        items = items
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.pending_detail_error_load)) }
            }
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) = _uiState.update { it.copy(paymentMethod = method) }

    fun markSalePaid(saleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                markPaid(listOf(saleId))
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.pending_detail_error_mark_paid)) }
            }
        }
    }

    fun markAllPaid() {
        val saleIds = _uiState.value.items.map { it.saleId }
        if (saleIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                markPaid(saleIds)
                load()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.pending_detail_error_mark_all_paid)) }
            }
        }
    }

    private suspend fun markPaid(saleIds: List<String>) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val paymentMethod = _uiState.value.paymentMethod
        saleIds.forEach { saleId ->
            val sale = saleRepository.getById(saleId) ?: return@forEach
            saleRepository.update(
                saleId,
                sale.copy(paymentStatus = PaymentStatus.PAID, paidDate = today, paymentMethod = paymentMethod)
            )
        }
    }
}
