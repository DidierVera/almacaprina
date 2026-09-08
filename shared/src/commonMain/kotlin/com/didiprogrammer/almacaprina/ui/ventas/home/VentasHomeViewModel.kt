package com.didiprogrammer.almacaprina.ui.ventas.home

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_error_load_failed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.pendingBalancesByCustomer
import com.didiprogrammer.almacaprina.business.pendingSalesBalance
import com.didiprogrammer.almacaprina.business.totalPackagingUnitsOut
import com.didiprogrammer.almacaprina.business.totalValue
import com.didiprogrammer.almacaprina.data.remote.AuthService
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
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
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

/** Bundle de los datos crudos que necesita Inicio de Ventas — se piden todos en paralelo. */
private data class VentasHomeRawData(
    val products: List<Product>,
    val sales: List<Sale>,
    val packagings: List<Packaging>,
    val depositTransactions: List<PackagingDepositTransaction>,
    val currency: String
)

/** Sección Ventas — Inicio. Ver mockup Ventas-selection.png. */
class VentasHomeViewModel(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val packagingRepository: PackagingRepository,
    private val packagingDepositTransactionRepository: PackagingDepositTransactionRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentasHomeUiState())
    val uiState: StateFlow<VentasHomeUiState> = _uiState.asStateFlow()

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

                // Los 5 repositorios son independientes — se piden todos a la vez.
                val raw = coroutineScope {
                    val productsDeferred = async { productRepository.getAll() }
                    val salesDeferred = async { saleRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val depositsDeferred = async { packagingDepositTransactionRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    VentasHomeRawData(
                        products = productsDeferred.await(),
                        sales = salesDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        depositTransactions = depositsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }

                val packagingsById = raw.packagings.associateBy { it.id }
                val featuredProduct = raw.products.firstOrNull { it.active && it.category == ProductCategory.RAW_MILK }
                    ?: raw.products.firstOrNull { it.active }

                val pendingBalances = pendingBalancesByCustomer(raw.sales, packagingsById)
                val litersSoldToday = raw.sales
                    .filter { it.date == today && it.productId == featuredProduct?.id }
                    .sumOf { it.quantitySold }
                val collectedToday = raw.sales
                    .filter { it.paidDate == today }
                    .sumOf { sale -> sale.totalValue(sale.packagingId?.let { packagingsById[it] }) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        currency = raw.currency,
                        featuredProduct = featuredProduct,
                        pendingTotal = pendingSalesBalance(raw.sales, packagingsById),
                        pendingCustomerCount = pendingBalances.size,
                        litersSoldToday = litersSoldToday,
                        collectedToday = collectedToday,
                        packagingDepositsOut = totalPackagingUnitsOut(raw.depositTransactions)
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.common_error_load_failed)) }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            AuthService.signOut()
            onLoggedOut()
        }
    }
}
