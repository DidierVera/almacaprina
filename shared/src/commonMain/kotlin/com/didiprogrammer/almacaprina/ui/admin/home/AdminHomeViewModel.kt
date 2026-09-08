package com.didiprogrammer.almacaprina.ui.admin.home

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.admin_home_alert_birth_expected
import almacaprina.shared.generated.resources.admin_home_alert_health_upcoming
import almacaprina.shared.generated.resources.health_record_type_deworming
import almacaprina.shared.generated.resources.health_record_type_diagnosis
import almacaprina.shared.generated.resources.health_record_type_routine_checkup
import almacaprina.shared.generated.resources.health_record_type_treatment
import almacaprina.shared.generated.resources.health_record_type_vaccine
import almacaprina.shared.generated.resources.admin_home_alert_insumo_low_stock_one
import almacaprina.shared.generated.resources.admin_home_alert_insumo_low_stock_other
import almacaprina.shared.generated.resources.admin_home_alert_weighing_overdue
import almacaprina.shared.generated.resources.common_error_load_failed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.didiprogrammer.almacaprina.business.costPerLiter
import com.didiprogrammer.almacaprina.business.currentHerdStatusCounts
import com.didiprogrammer.almacaprina.business.expectedBirthDateOrNull
import com.didiprogrammer.almacaprina.business.insumoLowStockAlerts
import com.didiprogrammer.almacaprina.business.overdueWeighings
import com.didiprogrammer.almacaprina.business.pendingSalesBalance
import com.didiprogrammer.almacaprina.business.rawMilkAvailableBalance
import com.didiprogrammer.almacaprina.business.revenueForPeriod
import com.didiprogrammer.almacaprina.business.totalLitersDay
import com.didiprogrammer.almacaprina.business.upcomingBirths
import com.didiprogrammer.almacaprina.business.upcomingHealthAlerts
import com.didiprogrammer.almacaprina.domain.model.BusinessSettings
import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.HealthRecordType
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.model.ProductCategory
import com.didiprogrammer.almacaprina.domain.model.ProductionBatch
import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchInsumoUsageRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString

/** Bundle de los datos crudos que necesita "Inicio" — se piden todos en paralelo, ver [AdminHomeViewModel.load]. */
private data class AdminHomeRawData(
    val businessSettings: BusinessSettings?,
    val goats: List<Goat>,
    val milkRecords: List<MilkProductionRecord>,
    val sales: List<Sale>,
    val purchases: List<Purchase>,
    val healthRecords: List<HealthRecord>,
    val feedingRecords: List<FeedingRecord>,
    val weightRecords: List<WeightRecord>,
    val reproductiveEvents: List<ReproductiveEvent>,
    val insumos: List<Insumo>,
    val careTasks: List<CareTask>,
    val products: List<Product>,
    val packagings: List<Packaging>,
    val productionBatches: List<ProductionBatch>,
    val batchUsages: List<ProductionBatchInsumoUsage>
)

/**
 * ViewModel de "Inicio" (vista general) del rol Compras/Admin.
 * Trae los datos crudos de cada repositorio y delega TODO cálculo derivado
 * (producción vs. meta, litros disponibles, costo por litro, alertas, etc.)
 * a shared/business/ — este archivo solo orquesta y arma el UiState.
 */
class AdminHomeViewModel(
    private val businessSettingsRepository: BusinessSettingsRepository,
    private val goatRepository: GoatRepository,
    private val milkProductionRecordRepository: MilkProductionRecordRepository,
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val feedingRecordRepository: FeedingRecordRepository,
    private val weightRecordRepository: WeightRecordRepository,
    private val reproductiveEventRepository: ReproductiveEventRepository,
    private val insumoRepository: InsumoRepository,
    private val careTaskRepository: CareTaskRepository,
    private val productRepository: ProductRepository,
    private val packagingRepository: PackagingRepository,
    private val productionBatchRepository: ProductionBatchRepository,
    private val productionBatchInsumoUsageRepository: ProductionBatchInsumoUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    // Datos crudos cacheados en memoria para recalcular el resumen financiero
    // al cambiar de período sin volver a golpear la red.
    private var cachedSales: List<Sale> = emptyList()
    private var cachedPackagingsById: Map<String, Packaging> = emptyMap()
    private var cachedPurchases: List<Purchase> = emptyList()
    private var cachedHealthRecords: List<HealthRecord> = emptyList()
    private var cachedFeedingRecords: List<FeedingRecord> = emptyList()
    private var cachedMilkRecords: List<MilkProductionRecord> = emptyList()
    private var today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    init {
        load()
    }

    fun load() = fetchData(isRefresh = false)

    /** Refresco por gesto de "pull to refresh" — deja la lista visible, solo muestra el indicador chico. */
    fun refresh() = fetchData(isRefresh = true)

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, errorMessage = null) }
            try {
                today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                // Los 15 repositorios son independientes entre sí — se piden todos a la
                // vez (en vez de uno tras otro) para que el tiempo total sea el de la
                // consulta más lenta, no la suma de las 15.
                val (
                    businessSettings, goats, milkRecords, sales, purchases,
                    healthRecords, feedingRecords, weightRecords, reproductiveEvents, insumos,
                    careTasks, products, packagings, productionBatches, batchUsages
                ) = coroutineScope {
                    val businessSettingsDeferred = async { businessSettingsRepository.getAll().firstOrNull() }
                    val goatsDeferred = async { goatRepository.getAll() }
                    val milkRecordsDeferred = async { milkProductionRecordRepository.getAll() }
                    val salesDeferred = async { saleRepository.getAll() }
                    val purchasesDeferred = async { purchaseRepository.getAll() }
                    val healthRecordsDeferred = async { healthRecordRepository.getAll() }
                    val feedingRecordsDeferred = async { feedingRecordRepository.getAll() }
                    val weightRecordsDeferred = async { weightRecordRepository.getAll() }
                    val reproductiveEventsDeferred = async { reproductiveEventRepository.getAll() }
                    val insumosDeferred = async { insumoRepository.getAll() }
                    val careTasksDeferred = async { careTaskRepository.getAll() }
                    val productsDeferred = async { productRepository.getAll() }
                    val packagingsDeferred = async { packagingRepository.getAll() }
                    val productionBatchesDeferred = async { productionBatchRepository.getAll() }
                    val batchUsagesDeferred = async { productionBatchInsumoUsageRepository.getAll() }

                    AdminHomeRawData(
                        businessSettings = businessSettingsDeferred.await(),
                        goats = goatsDeferred.await(),
                        milkRecords = milkRecordsDeferred.await(),
                        sales = salesDeferred.await(),
                        purchases = purchasesDeferred.await(),
                        healthRecords = healthRecordsDeferred.await(),
                        feedingRecords = feedingRecordsDeferred.await(),
                        weightRecords = weightRecordsDeferred.await(),
                        reproductiveEvents = reproductiveEventsDeferred.await(),
                        insumos = insumosDeferred.await(),
                        careTasks = careTasksDeferred.await(),
                        products = productsDeferred.await(),
                        packagings = packagingsDeferred.await(),
                        productionBatches = productionBatchesDeferred.await(),
                        batchUsages = batchUsagesDeferred.await()
                    )
                }

                cachedSales = sales
                cachedPackagingsById = packagings.associateBy { it.id }
                cachedPurchases = purchases
                cachedHealthRecords = healthRecords
                cachedFeedingRecords = feedingRecords
                cachedMilkRecords = milkRecords

                val milkProductId = products.firstOrNull { it.category == ProductCategory.RAW_MILK }?.id

                val todayLiters = milkRecords.filter { it.date == today }.sumOf { it.totalLitersDay() }
                val availableLiters = if (milkProductId != null) {
                    rawMilkAvailableBalance(milkRecords, sales, milkProductId, productionBatches)
                } else {
                    0.0
                }

                val herdCounts = herdCounts(goats)
                val alerts = buildAlerts(
                    healthRecords, weightRecords, goats, reproductiveEvents,
                    insumos, purchases, feedingRecords, batchUsages, careTasks
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        farmName = businessSettings?.farmName ?: "",
                        currency = businessSettings?.currency ?: "COP",
                        todayLiters = todayLiters,
                        targetLiters = businessSettings?.targetDailyLitersGoal ?: 0.0,
                        availableLiters = availableLiters,
                        herdCounts = herdCounts,
                        alerts = alerts
                    )
                }
                recomputeFinancialSummary(_uiState.value.financialPeriod)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = t.message ?: getString(Res.string.common_error_load_failed)) }
            }
        }
    }

    fun onFinancialPeriodSelected(period: FinancialPeriod) {
        _uiState.update { it.copy(financialPeriod = period) }
        recomputeFinancialSummary(period)
    }

    private fun recomputeFinancialSummary(period: FinancialPeriod) {
        val start = when (period) {
            FinancialPeriod.TODAY -> today
            FinancialPeriod.WEEK -> today.minus(6, DateTimeUnit.DAY)
            FinancialPeriod.MONTH -> today.minus(29, DateTimeUnit.DAY)
        }
        val revenue = revenueForPeriod(cachedSales, start, today)
        val pendingReceivable = pendingSalesBalance(cachedSales, cachedPackagingsById)
        val litersInPeriod = cachedMilkRecords
            .filter { it.date >= start && it.date <= today }
            .sumOf { it.totalLitersDay() }
        val purchasesInPeriod = cachedPurchases.filter { it.date >= start && it.date <= today }
        val healthInPeriod = cachedHealthRecords.filter { it.date >= start && it.date <= today }
        val feedingInPeriod = cachedFeedingRecords.filter { it.date >= start && it.date <= today }
        val costPerLiterValue = costPerLiter(purchasesInPeriod, healthInPeriod, feedingInPeriod, litersInPeriod)

        _uiState.update {
            it.copy(
                revenue = revenue,
                pendingReceivable = pendingReceivable,
                costPerLiter = costPerLiterValue
            )
        }
    }

    private fun herdCounts(goats: List<Goat>): HerdStatusCounts {
        val counts = currentHerdStatusCounts(goats)
        return HerdStatusCounts(
            inProduction = counts[GoatStatus.IN_PRODUCTION] ?: 0,
            pregnant = counts[GoatStatus.PREGNANT] ?: 0,
            dry = counts[GoatStatus.DRY] ?: 0,
            youngDoes = counts[GoatStatus.YOUNG_DOE] ?: 0
        )
    }

    private suspend fun buildAlerts(
        healthRecords: List<HealthRecord>,
        weightRecords: List<WeightRecord>,
        goats: List<Goat>,
        reproductiveEvents: List<ReproductiveEvent>,
        insumos: List<Insumo>,
        purchases: List<Purchase>,
        feedingRecords: List<FeedingRecord>,
        batchUsages: List<ProductionBatchInsumoUsage>,
        careTasks: List<CareTask>
    ): List<HomeAlert> {
        val alerts = mutableListOf<HomeAlert>()

        upcomingHealthAlerts(healthRecords, today).forEach { record ->
            val label = getString(
                when (record.type) {
                    HealthRecordType.VACCINE -> Res.string.health_record_type_vaccine
                    HealthRecordType.DEWORMING -> Res.string.health_record_type_deworming
                    HealthRecordType.TREATMENT -> Res.string.health_record_type_treatment
                    HealthRecordType.ROUTINE_CHECKUP -> Res.string.health_record_type_routine_checkup
                    HealthRecordType.DIAGNOSIS -> Res.string.health_record_type_diagnosis
                }
            )
            alerts += HomeAlert(
                id = "health_${record.id}",
                type = HomeAlertType.VACCINE,
                message = getString(Res.string.admin_home_alert_health_upcoming, label, record.nextSuggestedDate.toString())
            )
        }

        overdueWeighings(goats, weightRecords, today).forEach { goat ->
            alerts += HomeAlert(
                id = "weighing_${goat.id}",
                type = HomeAlertType.WEIGHING,
                message = getString(Res.string.admin_home_alert_weighing_overdue, goat.name, goat.tagNumber)
            )
        }

        upcomingBirths(reproductiveEvents, today).forEach { event ->
            val expectedDate = expectedBirthDateOrNull(event.eventType, event.date)
            alerts += HomeAlert(
                id = "birth_${event.id}",
                type = HomeAlertType.BIRTH,
                message = getString(Res.string.admin_home_alert_birth_expected, expectedDate.toString())
            )
        }

        insumoLowStockAlerts(insumos, purchases, feedingRecords, healthRecords, batchUsages, careTasks).forEach { alert ->
            val days = alert.daysRemaining.toInt()
            val template = if (days == 1) Res.string.admin_home_alert_insumo_low_stock_one else Res.string.admin_home_alert_insumo_low_stock_other
            alerts += HomeAlert(
                id = "insumo_${alert.insumo.id}",
                type = HomeAlertType.INSUMO,
                message = getString(template, alert.insumo.name, days)
            )
        }

        return alerts
    }
}
