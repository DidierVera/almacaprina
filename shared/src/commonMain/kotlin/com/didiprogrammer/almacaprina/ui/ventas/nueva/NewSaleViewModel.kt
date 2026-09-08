package com.didiprogrammer.almacaprina.ui.ventas.nueva

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.common_error_load_failed
import almacaprina.shared.generated.resources.new_sale_error_create_customer
import almacaprina.shared.generated.resources.new_sale_error_save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.model.CustomerType
import com.didiprogrammer.almacaprina.domain.model.DepositMovementType
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.model.PaymentMethod
import com.didiprogrammer.almacaprina.domain.model.PaymentStatus
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository
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

/** Bundle de los datos crudos que necesita el wizard — se piden todos en paralelo. */
private data class NewSaleRawData(
    val customers: List<Customer>,
    val sales: List<Sale>,
    val products: List<Product>,
    val packagings: List<com.didiprogrammer.almacaprina.domain.model.Packaging>,
    val currency: String
)

/**
 * ViewModel único para los 4 pasos de "Nueva venta" — se resuelve con el mismo `viewModelStoreOwner`
 * (el back stack entry del grafo anidado `VentasRoutes.NEW_SALE_GRAPH`) en las 4 pantallas, así que
 * una sola instancia sobrevive mientras el usuario navega entre pasos con "Volver"/"Continuar".
 */
class NewSaleViewModel(
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val packagingRepository: PackagingRepository,
    private val packagingDepositTransactionRepository: PackagingDepositTransactionRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewSaleUiState())
    val uiState: StateFlow<NewSaleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val raw = coroutineScope {
                    val customersDeferred = async { customerRepository.getAll() }
                    val salesDeferred = async { saleRepository.getAll() }
                    val productsDeferred = async { productRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    NewSaleRawData(
                        customers = customersDeferred.await(),
                        sales = salesDeferred.await(),
                        products = productsDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val lastSaleByCustomer = raw.sales
                    .groupBy { it.customerId }
                    .mapValues { (_, sales) -> sales.maxOf { it.date } }
                val defaultPackagingId = raw.packagings.firstOrNull { !it.isReturnable }?.id
                    ?: raw.packagings.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        today = today,
                        currency = raw.currency,
                        customers = raw.customers.sortedByDescending { c -> lastSaleByCustomer[c.id] },
                        lastSaleDateByCustomer = lastSaleByCustomer,
                        activeProducts = raw.products.filter { p -> p.active },
                        packagings = raw.packagings,
                        selectedPackagingId = defaultPackagingId
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: getString(Res.string.common_error_load_failed)) }
            }
        }
    }

    // ---------- Paso 1 — ¿Para quién? ----------
    fun onCustomerSearchChanged(query: String) = _uiState.update { it.copy(customerSearchQuery = query) }
    fun onCustomerSelected(customer: Customer) = _uiState.update { it.copy(selectedCustomer = customer, showNewCustomerForm = false) }
    fun onToggleNewCustomerForm(show: Boolean) = _uiState.update { it.copy(showNewCustomerForm = show) }
    fun onNewCustomerNameChanged(value: String) = _uiState.update { it.copy(newCustomerName = value) }
    fun onNewCustomerContactChanged(value: String) = _uiState.update { it.copy(newCustomerContact = value) }

    fun createCustomerAndContinue(onDone: () -> Unit) {
        val state = _uiState.value
        if (!state.canCreateNewCustomer) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val customer = customerRepository.insert(
                    Customer(
                        id = newId(),
                        name = state.newCustomerName.trim(),
                        type = CustomerType.INDIVIDUAL,
                        contact = state.newCustomerContact.trim().ifBlank { null }
                    )
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        customers = it.customers + customer,
                        selectedCustomer = customer,
                        showNewCustomerForm = false,
                        newCustomerName = "",
                        newCustomerContact = ""
                    )
                }
                onDone()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.new_sale_error_create_customer)) }
            }
        }
    }

    // ---------- Paso 2 — ¿Qué vas a vender? ----------
    fun onProductSelected(product: Product) = _uiState.update { it.copy(selectedProduct = product) }

    // ---------- Paso 3 — Cantidad y envase ----------
    fun onQuantityChanged(value: String) = _uiState.update { it.copy(quantityText = value) }
    fun onPackagingSelected(packagingId: String) = _uiState.update { it.copy(selectedPackagingId = packagingId) }

    // ---------- Paso 4 — Confirmar ----------
    fun onPaymentMethodSelected(method: PaymentMethod) = _uiState.update { it.copy(paymentMethod = method) }
    fun onPaymentStatusSelected(status: PaymentStatus) = _uiState.update { it.copy(paymentStatus = status) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val customer = state.selectedCustomer ?: return
        val product = state.selectedProduct ?: return
        val quantity = state.quantity ?: return
        val today = state.today ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val saleId = newId()
                val packaging = state.selectedPackaging
                val isReturnable = packaging?.isReturnable == true
                saleRepository.insert(
                    Sale(
                        id = saleId,
                        date = today,
                        customerId = customer.id,
                        productId = product.id,
                        quantitySold = quantity,
                        unitPrice = product.defaultUnitPrice,
                        packagingId = packaging?.id,
                        newPackagingUnitsCount = if (packaging != null) state.newPackagingUnitsCount else null,
                        packagingReturnedCount = null,
                        paymentMethod = state.paymentMethod,
                        paymentStatus = state.paymentStatus,
                        paidDate = if (state.paymentStatus == PaymentStatus.PAID) today else null
                    )
                )
                // Si el envase es retornable, se registra el movimiento de depósito
                // correspondiente — mismo patrón que Purchase -> PackagingInventory en Admin.
                if (isReturnable && state.newPackagingUnitsCount > 0) {
                    packagingDepositTransactionRepository.insert(
                        PackagingDepositTransaction(
                            id = newId(),
                            customerId = customer.id,
                            saleId = saleId,
                            packagingId = packaging.id,
                            date = today,
                            movementType = DepositMovementType.DEPOSIT_CHARGED,
                            quantity = state.newPackagingUnitsCount
                        )
                    )
                }
                _uiState.update { it.copy(isSaving = false) }
                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.new_sale_error_save)) }
            }
        }
    }
}
