package com.didiprogrammer.almacaprina.ui.admin.compras

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_new_purchase_error_save
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.domain.model.PackagingInventory
import com.didiprogrammer.almacaprina.domain.model.PackagingMovementType
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.PurchaseCategory
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingInventoryRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import com.didiprogrammer.almacaprina.util.newId
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

/**
 * Sección 5, pantalla 5.2 — Nueva compra. Implementa las dos reglas de negocio
 * confirmadas en CLAUDE.md: actualizar el costo del insumo/envase comprado, y —
 * si es un envase — generar el movimiento correspondiente en PackagingInventory.
 */
class AdminNewPurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val insumoRepository: InsumoRepository,
    private val packagingRepository: PackagingRepository,
    private val packagingInventoryRepository: PackagingInventoryRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNewPurchaseUiState())
    val uiState: StateFlow<AdminNewPurchaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            // Los 3 repositorios son independientes — se piden todos a la vez.
            val (insumos, packagings, currency) = coroutineScope {
                val insumosDeferred = async { insumoRepository.getAll() }
                val packagingsDeferred = async { packagingRepository.getAll() }
                val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }
                Triple(insumosDeferred.await(), packagingsDeferred.await(), currencyDeferred.await())
            }
            _uiState.update {
                it.copy(isLoading = false, insumos = insumos, packagings = packagings, date = today, currency = currency)
            }
        }
    }

    fun onCategoryChanged(category: PurchaseCategory) =
        _uiState.update { it.copy(category = category, selectedInsumoId = null, selectedPackagingId = null) }

    fun onInsumoSelected(insumoId: String) = _uiState.update {
        it.copy(selectedInsumoId = insumoId, purchaseByPackage = false, packageCountText = "", packageCostText = "")
    }
    fun onPackagingSelected(packagingId: String) = _uiState.update { it.copy(selectedPackagingId = packagingId) }
    fun onDateChanged(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onSupplierChanged(value: String) = _uiState.update { it.copy(supplier = value) }
    fun onQuantityChanged(value: String) = _uiState.update { it.copy(quantityText = value) }
    fun onUnitCostChanged(value: String) = _uiState.update { it.copy(unitCostText = value) }
    fun onPurchaseByPackageToggled(value: Boolean) = _uiState.update { it.copy(purchaseByPackage = value) }
    fun onPackageCountChanged(value: String) = _uiState.update { it.copy(packageCountText = value) }
    fun onPackageCostChanged(value: String) = _uiState.update { it.copy(packageCostText = value) }
    fun onNotesChanged(value: String) = _uiState.update { it.copy(notes = value) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return
        val quantity = state.quantity ?: return
        val unitCost = state.unitCost ?: return
        val date = state.date ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                purchaseRepository.insert(
                    Purchase(
                        id = newId(),
                        date = date,
                        category = state.category,
                        insumoId = if (state.category != PurchaseCategory.PACKAGING) state.selectedInsumoId else null,
                        packagingId = if (state.category == PurchaseCategory.PACKAGING) state.selectedPackagingId else null,
                        supplier = state.supplier.ifBlank { null },
                        quantity = quantity,
                        unit = null,
                        unitCost = unitCost,
                        notes = state.notes.ifBlank { null }
                    )
                )

                if (state.category == PurchaseCategory.PACKAGING) {
                    val packagingId = state.selectedPackagingId!!
                    // Genera automáticamente el movimiento de inventario de envases.
                    packagingInventoryRepository.insert(
                        PackagingInventory(
                            id = newId(),
                            date = date,
                            packagingId = packagingId,
                            movementType = PackagingMovementType.PURCHASE,
                            quantity = quantity.toInt(),
                            unitCost = unitCost
                        )
                    )
                    // Actualiza el costo del envase.
                    val packaging = packagingRepository.getById(packagingId)
                    if (packaging != null) {
                        packagingRepository.update(packagingId, packaging.copy(unitCost = unitCost))
                    }
                } else {
                    val insumoId = state.selectedInsumoId!!
                    // Actualiza Insumo.last_unit_cost.
                    val insumo = insumoRepository.getById(insumoId)
                    if (insumo != null) {
                        insumoRepository.update(insumoId, insumo.copy(lastUnitCost = unitCost))
                    }
                }

                onSaved()
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: getString(Res.string.admin_new_purchase_error_save)) }
            }
        }
    }
}
