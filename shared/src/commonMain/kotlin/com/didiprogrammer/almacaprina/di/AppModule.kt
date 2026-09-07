package com.didiprogrammer.almacaprina.di

import com.didiprogrammer.almacaprina.data.repository.BusinessSettingsRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.CareTaskLogRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.CareTaskRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.CustomerRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.FeedingRecordRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.GoatRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.HealthRecordRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.InsumoRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.MilkProductionRecordRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.PackagingDepositTransactionRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.PackagingInventoryRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.PackagingRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.ProductRecipeItemRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.ProductRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.ProductionBatchInsumoUsageRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.ProductionBatchRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.PurchaseRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.ReproductiveEventRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.SaleRepositoryImpl
import com.didiprogrammer.almacaprina.data.repository.WeightRecordRepositoryImpl
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository
import com.didiprogrammer.almacaprina.domain.repository.CareTaskLogRepository
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingInventoryRepository
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRecipeItemRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchInsumoUsageRepository
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository
import com.didiprogrammer.almacaprina.ui.admin.ajustes.AdminSettingsViewModel
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminGoatDetailViewModel
import com.didiprogrammer.almacaprina.ui.admin.calendario.AdminCareTaskFormViewModel
import com.didiprogrammer.almacaprina.ui.admin.calendario.AdminCareTaskListViewModel
import com.didiprogrammer.almacaprina.ui.admin.catalogo.AdminCatalogoViewModel
import com.didiprogrammer.almacaprina.ui.admin.compras.AdminNewPurchaseViewModel
import com.didiprogrammer.almacaprina.ui.admin.compras.AdminPurchaseHistoryViewModel
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminHatoListViewModel
import com.didiprogrammer.almacaprina.ui.admin.hato.AdminGoatFormViewModel
import com.didiprogrammer.almacaprina.ui.admin.home.AdminHomeViewModel
import com.didiprogrammer.almacaprina.ui.admin.produccion.AdminNewBatchViewModel
import com.didiprogrammer.almacaprina.ui.admin.produccion.AdminProductionHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo raíz de inyección de dependencias (Koin). Registra un repositorio
 * por cada entidad del modelo de datos (ver docs/data_model.md), enlazando
 * la interfaz de domain/repository con su implementación de data/repository.
 */
val appModule = module {
    single<GoatRepository> { GoatRepositoryImpl() }
    single<WeightRecordRepository> { WeightRecordRepositoryImpl() }
    single<ReproductiveEventRepository> { ReproductiveEventRepositoryImpl() }
    single<MilkProductionRecordRepository> { MilkProductionRecordRepositoryImpl() }
    single<HealthRecordRepository> { HealthRecordRepositoryImpl() }
    single<FeedingRecordRepository> { FeedingRecordRepositoryImpl() }
    single<InsumoRepository> { InsumoRepositoryImpl() }
    single<ProductRecipeItemRepository> { ProductRecipeItemRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<PackagingRepository> { PackagingRepositoryImpl() }
    single<ProductionBatchRepository> { ProductionBatchRepositoryImpl() }
    single<ProductionBatchInsumoUsageRepository> { ProductionBatchInsumoUsageRepositoryImpl() }
    single<PurchaseRepository> { PurchaseRepositoryImpl() }
    single<CustomerRepository> { CustomerRepositoryImpl() }
    single<SaleRepository> { SaleRepositoryImpl() }
    single<PackagingInventoryRepository> { PackagingInventoryRepositoryImpl() }
    single<PackagingDepositTransactionRepository> { PackagingDepositTransactionRepositoryImpl() }
    single<BusinessSettingsRepository> { BusinessSettingsRepositoryImpl() }
    single<CareTaskRepository> { CareTaskRepositoryImpl() }
    single<CareTaskLogRepository> { CareTaskLogRepositoryImpl() }

    viewModel {
        AdminHomeViewModel(
            businessSettingsRepository = get(),
            goatRepository = get(),
            milkProductionRecordRepository = get(),
            saleRepository = get(),
            purchaseRepository = get(),
            healthRecordRepository = get(),
            feedingRecordRepository = get(),
            weightRecordRepository = get(),
            reproductiveEventRepository = get(),
            insumoRepository = get(),
            careTaskRepository = get(),
            productRepository = get(),
            packagingRepository = get(),
            productionBatchRepository = get(),
            productionBatchInsumoUsageRepository = get()
        )
    }

    viewModel { params ->
        AdminHatoListViewModel(
            initialStatusFilter = params.getOrNull(),
            goatRepository = get(),
            milkProductionRecordRepository = get(),
            reproductiveEventRepository = get(),
            weightRecordRepository = get()
        )
    }

    viewModel { params ->
        AdminGoatDetailViewModel(
            goatId = params.get(),
            goatRepository = get(),
            weightRecordRepository = get(),
            reproductiveEventRepository = get(),
            healthRecordRepository = get(),
            milkProductionRecordRepository = get(),
            insumoRepository = get()
        )
    }

    viewModel { params -> AdminGoatFormViewModel(goatId = params.getOrNull(), goatRepository = get()) }

    viewModel {
        AdminCatalogoViewModel(
            productRepository = get(),
            packagingRepository = get(),
            insumoRepository = get(),
            productRecipeItemRepository = get(),
            businessSettingsRepository = get()
        )
    }

    viewModel {
        AdminProductionHistoryViewModel(
            productionBatchRepository = get(),
            productRepository = get()
        )
    }

    viewModel {
        AdminNewBatchViewModel(
            productRepository = get(),
            milkProductionRecordRepository = get(),
            saleRepository = get(),
            productionBatchRepository = get(),
            productionBatchInsumoUsageRepository = get(),
            productRecipeItemRepository = get(),
            insumoRepository = get(),
            purchaseRepository = get(),
            healthRecordRepository = get(),
            feedingRecordRepository = get(),
            businessSettingsRepository = get()
        )
    }

    viewModel {
        AdminPurchaseHistoryViewModel(
            purchaseRepository = get(),
            insumoRepository = get(),
            packagingRepository = get(),
            businessSettingsRepository = get()
        )
    }

    viewModel {
        AdminNewPurchaseViewModel(
            purchaseRepository = get(),
            insumoRepository = get(),
            packagingRepository = get(),
            packagingInventoryRepository = get(),
            businessSettingsRepository = get()
        )
    }

    viewModel {
        AdminCareTaskListViewModel(
            careTaskRepository = get(),
            insumoRepository = get()
        )
    }

    viewModel { params ->
        AdminCareTaskFormViewModel(
            taskId = params.getOrNull(),
            careTaskRepository = get(),
            insumoRepository = get()
        )
    }

    viewModel {
        AdminSettingsViewModel(
            businessSettingsRepository = get(),
            purchaseRepository = get(),
            healthRecordRepository = get(),
            feedingRecordRepository = get(),
            milkProductionRecordRepository = get()
        )
    }
}
