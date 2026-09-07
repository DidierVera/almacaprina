package com.didiprogrammer.almacaprina.ui.admin.produccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.averageYieldRatio
import com.didiprogrammer.almacaprina.business.costPerLiter
import com.didiprogrammer.almacaprina.business.rawMilkAvailableBalance
import com.didiprogrammer.almacaprina.business.totalLitersDay
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.ProductionBatch
import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRecipeItemRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchInsumoUsageRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.ui.components.label
import com.didiprogrammer.almacaprina.util.newId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Bundle de los datos crudos que necesita el wizard — se piden todos en paralelo, ver [AdminNewBatchViewModel.load]. */
private data class AdminNewBatchRawData(
    val products: List<Product>,
    val milkRecords: List<MilkProductionRecord>,
    val sales: List<Sale>,
    val batches: List<ProductionBatch>,
    val insumos: List<Insumo>,
    val recipeItems: List<ProductRecipeItem>,
    val purchases: List<Purchase>,
    val healthRecords: List<HealthRecord>,
    val feedingRecords: List<FeedingRecord>,
    val currency: String
)

/**
 * Sección 4, pantalla 4.2 — Flujo de varios pasos para registrar un lote de producción.
 * Un solo ViewModel para todo el wizard: es un único formulario largo, no pantallas
 * independientes con su propio ciclo de vida.
 */
class AdminNewBatchViewModel(
    private val productRepository: ProductRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val saleRepository: SaleRepository,
    private val productionBatchRepository: ProductionBatchRepository,
    private val productionBatchInsumoUsageRepository: ProductionBatchInsumoUsageRepository,
    private val productRecipeItemRepository: ProductRecipeItemRepository,
    private val insumoRepository: InsumoRepository,
    private val purchaseRepository: PurchaseRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val feedingRecordRepository: FeedingRecordRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNewBatchUiState())
    val uiState: StateFlow<AdminNewBatchUiState> = _uiState.asStateFlow()

    private var allBatches: List<ProductionBatch> = emptyList()
    private var recipeItemsByProduct: Map<String, List<Pair<String, Double>>> = emptyMap() // productId -> (insumoId, qtyPerUnit)
    private var insumosById: Map<String, Insumo> = emptyMap()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val start = today.minus(29, DateTimeUnit.DAY)

                // Los repositorios son independientes entre sí — se piden todos a la vez
                // (en vez de uno tras otro) para que el tiempo total sea el de la consulta
                // más lenta, no la suma de todas.
                val raw = coroutineScope {
                    val productsDeferred = async { productRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val salesDeferred = async { saleRepository.getAll() }
                    val batchesDeferred = async { productionBatchRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    val recipeItemsDeferred = async { productRecipeItemRepository.getAll() }
                    val purchasesDeferred = async { purchaseRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    val feedingRecordsDeferred = async { feedingRecordRepository.getAll() }
                    val currencyDeferred = async { businessSettingsRepository.getAll().firstOrNull()?.currency ?: "COP" }

                    AdminNewBatchRawData(
                        products = productsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await(),
                        sales = salesDeferred.await(),
                        batches = batchesDeferred.await(),
                        insumos = insumosDeferred.await(),
                        recipeItems = recipeItemsDeferred.await(),
                        purchases = purchasesDeferred.await(),
                        healthRecords = healthRecordsDeferred.await(),
                        feedingRecords = feedingRecordsDeferred.await(),
                        currency = currencyDeferred.await()
                    )
                }

                val products = raw.products
                val derivedActive = products.filter { it.category == ProductCategory.DERIVED_DAIRY && it.active }
                val milkRecords = raw.milkRecords
                val sales = raw.sales
                allBatches = raw.batches
                val insumos = raw.insumos
                insumosById = insumos.associateBy { it.id }
                val recipeItems = raw.recipeItems
                recipeItemsByProduct = recipeItems.groupBy({ it.productId }, { it.insumoId to it.quantityPerOutputUnit })
                val purchases = raw.purchases.filter { it.date >= start && it.date <= today }
                val healthRecords = raw.healthRecords.filter { it.date >= start && it.date <= today }
                val feedingRecords = raw.feedingRecords.filter { it.date >= start && it.date <= today }
                val litersInPeriod = milkRecords.filter { it.date >= start && it.date <= today }.sumOf { it.totalLitersDay() }
                val costPerLiterMilk = costPerLiter(purchases, healthRecords, feedingRecords, litersInPeriod)
                val currency = raw.currency

                val milkProductId = products.firstOrNull { it.category == ProductCategory.RAW_MILK }?.id
                val available = if (milkProductId != null) {
                    rawMilkAvailableBalance(milkRecords, sales, milkProductId, allBatches)
                } else {
                    0.0
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        derivedProducts = derivedActive,
                        allInsumos = insumos,
                        availableMilkLiters = available,
                        costPerLiterMilk = costPerLiterMilk,
                        currency = currency,
                        date = it.date ?: today
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = t.message ?: "No se pudo cargar la información") }
            }
        }
    }

    fun onProductSelected(product: Product) {
        val avgYield = averageYieldRatio(allBatches, product.id)
        _uiState.update { it.copy(selectedProduct = product, historicalAverageYield = avgYield) }
    }

    fun onMilkLitersChanged(value: Double) = _uiState.update { it.copy(milkLitersUsed = value.coerceAtLeast(0.0)) }
    fun onOutputQuantityChanged(text: String) = _uiState.update { it.copy(outputQuantityText = text) }
    fun onDateChanged(value: kotlinx.datetime.LocalDate) = _uiState.update { it.copy(date = value) }
    fun onResponsibleChanged(value: String) = _uiState.update { it.copy(responsible = value) }
    fun onNotesChanged(value: String) = _uiState.update { it.copy(notes = value) }

    fun onInsumoQuantityChanged(insumoId: String, newQuantity: Double) {
        _uiState.update { state ->
            state.copy(
                insumoUsages = state.insumoUsages.map { usage ->
                    if (usage.insumoId == insumoId) usage.copy(quantityUsed = newQuantity) else usage
                }
            )
        }
    }

    fun goNext() {
        val state = _uiState.value
        if (!state.canGoNext) return
        if (state.step == BatchWizardStep.CANTIDAD && state.insumoUsages.isEmpty()) {
            prefillInsumoUsages()
        }
        val nextIndex = (state.step.ordinal + 1).coerceAtMost(BatchWizardStep.entries.lastIndex)
        _uiState.update { it.copy(step = BatchWizardStep.entries[nextIndex]) }
    }

    fun goBack() {
        val previousIndex = (_uiState.value.step.ordinal - 1).coerceAtLeast(0)
        _uiState.update { it.copy(step = BatchWizardStep.entries[previousIndex]) }
    }

    private fun prefillInsumoUsages() {
        val state = _uiState.value
        val product = state.selectedProduct ?: return
        val outputQty = state.outputQuantity ?: return
        val recipe = recipeItemsByProduct[product.id].orEmpty()
        val usages = recipe.map { (insumoId, qtyPerUnit) ->
            val insumo = insumosById[insumoId]
            BatchInsumoUsageEntry(
                insumoId = insumoId,
                insumoName = insumo?.name ?: "Insumo eliminado",
                unitOfMeasureLabel = insumo?.unitOfMeasure?.label() ?: "",
                quantityUsed = qtyPerUnit * outputQty,
                unitCostAtTime = insumo?.lastUnitCost ?: 0.0
            )
        }
        _uiState.update { it.copy(insumoUsages = usages) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val product = state.selectedProduct ?: return
        val outputQty = state.outputQuantity ?: return
        val date = state.date ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val batchId = newId()
                productionBatchRepository.insert(
                    ProductionBatch(
                        id = batchId,
                        date = date,
                        outputProductId = product.id,
                        milkLitersUsed = state.milkLitersUsed,
                        outputQuantity = outputQty,
                        responsible = state.responsible.ifBlank { null },
                        notes = state.notes.ifBlank { null }
                    )
                )
                state.insumoUsages.forEach { usage ->
                    productionBatchInsumoUsageRepository.insert(
                        ProductionBatchInsumoUsage(
                            id = newId(),
                            productionBatchId = batchId,
                            insumoId = usage.insumoId,
                            quantityUsed = usage.quantityUsed,
                            unitCostAtTime = usage.unitCostAtTime
                        )
                    )
                }
                onSaved()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSaving = false, errorMessage = t.message ?: "No se pudo guardar el lote") }
            }
        }
    }
}
