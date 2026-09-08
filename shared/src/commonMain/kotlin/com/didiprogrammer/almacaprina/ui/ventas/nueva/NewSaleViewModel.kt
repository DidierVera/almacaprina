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
import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductPackagingOptionRepository
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
    val productPackagingOptions: List<ProductPackagingOption>,
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
    private val productPackagingOptionRepository: ProductPackagingOptionRepository,
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
                    val productPackagingOptionsDeferred = async { productPackagingOptionRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                    NewSaleRawData(
                        customers = customersDeferred.await(),
                        sales = salesDeferred.await(),
                        products = productsDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        productPackagingOptions = productPackagingOptionsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }
                val lastSaleByCustomer = raw.sales
                    .groupBy { it.customerId }
                    .mapValues { (_, sales) -> sales.maxOf { it.date } }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        today = today,
                        currency = raw.currency,
                        customers = raw.customers.sortedByDescending { c -> lastSaleByCustomer[c.id] },
                        lastSaleDateByCustomer = lastSaleByCustomer,
                        activeProducts = raw.products.filter { p -> p.active },
                        packagings = raw.packagings,
                        productPackagingOptions = raw.productPackagingOptions
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
    // Al elegir producto se preselecciona su envase predeterminado (Catálogo > Empaques,
    // Admin) — si no tiene ninguno configurado, se conserva el criterio anterior (el primero
    // no retornable, o el primero de todos) sobre el catálogo completo.
    fun onProductSelected(product: Product) = _uiState.update { state ->
        val options = state.productPackagingOptions.filter { it.productId == product.id }
        val candidates = if (options.isNotEmpty()) {
            val ids = options.map { it.packagingId }.toSet()
            state.packagings.filter { it.id in ids }
        } else {
            state.packagings
        }
        val defaultPackagingId = options.firstOrNull { it.isDefault }?.packagingId
            ?: candidates.firstOrNull { !it.isReturnable }?.id
            ?: candidates.firstOrNull()?.id
        state.copy(selectedProduct = product, selectedPackagingId = defaultPackagingId, newPackagingUnitsOverride = null)
    }

    // ---------- Paso 3 — Cantidad y envase ----------
    // Cambiar la cantidad o el envase invalida el ajuste manual de "envases nuevos entregados"
    // — vuelve a calcularse por defecto (1 por unidad vendida) hasta que el usuario lo ajuste de nuevo.
    fun onQuantityChanged(value: String) = _uiState.update { it.copy(quantityText = value, newPackagingUnitsOverride = null) }
    fun onPackagingSelected(packagingId: String) = _uiState.update { it.copy(selectedPackagingId = packagingId, newPackagingUnitsOverride = null) }
    fun onNewPackagingUnitsChanged(count: Int) = _uiState.update { it.copy(newPackagingUnitsOverride = count.coerceIn(0, it.quantity?.toInt() ?: 0)) }

    // ---------- Carrito (varios productos en una sola venta) ----------
    /** Confirma la línea en curso (producto + cantidad + envase) al carrito y limpia el
     * formulario para poder elegir el siguiente producto — usado tanto por "Agregar otro
     * producto" (vuelve al Paso 2) como por "Continuar" (avanza al Paso 4). */
    fun addCurrentLineToCart() {
        val state = _uiState.value
        val product = state.selectedProduct ?: return
        if (!state.canContinueFromCantidad) return
        val line = CartLine(
            product = product,
            quantityText = state.quantityText,
            packagingId = state.selectedPackagingId,
            newPackagingUnitsOverride = state.newPackagingUnitsOverride
        )
        _uiState.update {
            it.copy(
                cartLines = it.cartLines + line,
                selectedProduct = null,
                quantityText = "",
                selectedPackagingId = null,
                newPackagingUnitsOverride = null
            )
        }
    }

    /** Saca la línea del carrito y la vuelve a dejar "en curso" para editarla — la pantalla
     * navega de vuelta al Paso 2 (producto) para que el usuario la reconfigure y la vuelva a
     * confirmar con "Agregar otro producto"/"Continuar". */
    fun editCartLine(index: Int) {
        val line = _uiState.value.cartLines.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                cartLines = it.cartLines.filterIndexed { i, _ -> i != index },
                selectedProduct = line.product,
                quantityText = line.quantityText,
                selectedPackagingId = line.packagingId,
                newPackagingUnitsOverride = line.newPackagingUnitsOverride
            )
        }
    }

    fun removeCartLine(index: Int) = _uiState.update { it.copy(cartLines = it.cartLines.filterIndexed { i, _ -> i != index }) }

    // ---------- Paso 4 — Confirmar ----------
    fun onPaymentMethodSelected(method: PaymentMethod) = _uiState.update { it.copy(paymentMethod = method) }
    fun onPaymentStatusSelected(status: PaymentStatus) = _uiState.update { it.copy(paymentStatus = status) }

    /** Guarda una fila `Sale` por cada línea del carrito, todas con el mismo cliente/fecha/forma
     * de pago — para el resto de la app (Admin, Cobrar pendientes) son ventas normales, solo que
     * hechas en el mismo momento. Ver el punto del usuario: "no puedo vender 2 productos en la
     * misma venta" — Sale sigue siendo 1 fila = 1 producto (cero cambios en Admin), el carrito
     * solo agrupa varias inserciones desde este wizard. */
    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val customer = state.selectedCustomer ?: return
        if (state.cartLines.isEmpty()) return
        val today = state.today ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                state.cartLines.forEach { line ->
                    val saleId = newId()
                    val packaging = line.packaging(state.packagingsById)
                    val isReturnable = packaging?.isReturnable == true
                    val units = line.newPackagingUnitsCount()
                    saleRepository.insert(
                        Sale(
                            id = saleId,
                            date = today,
                            customerId = customer.id,
                            productId = line.product.id,
                            quantitySold = line.quantity ?: 0.0,
                            unitPrice = line.product.defaultUnitPrice,
                            packagingId = packaging?.id,
                            newPackagingUnitsCount = if (packaging != null) units else null,
                            packagingReturnedCount = null,
                            paymentMethod = state.paymentMethod,
                            paymentStatus = state.paymentStatus,
                            paidDate = if (state.paymentStatus == PaymentStatus.PAID) today else null
                        )
                    )
                    // Si el envase es retornable, se registra el movimiento de depósito
                    // correspondiente — mismo patrón que Purchase -> PackagingInventory en Admin.
                    if (isReturnable && units > 0) {
                        packagingDepositTransactionRepository.insert(
                            PackagingDepositTransaction(
                                id = newId(),
                                customerId = customer.id,
                                saleId = saleId,
                                packagingId = packaging.id,
                                date = today,
                                movementType = DepositMovementType.DEPOSIT_CHARGED,
                                quantity = units
                            )
                        )
                    }
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
