package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchInsumoUsageRepository

class ProductionBatchInsumoUsageRepositoryImpl : ProductionBatchInsumoUsageRepository {
    override suspend fun getAll(): List<ProductionBatchInsumoUsage> =
        fetchAll(SupabaseTables.PRODUCTION_BATCH_INSUMO_USAGES)

    override suspend fun getById(id: String): ProductionBatchInsumoUsage? =
        fetchById(SupabaseTables.PRODUCTION_BATCH_INSUMO_USAGES, id)

    override suspend fun insert(productionBatchInsumoUsage: ProductionBatchInsumoUsage): ProductionBatchInsumoUsage =
        insertRow(SupabaseTables.PRODUCTION_BATCH_INSUMO_USAGES, productionBatchInsumoUsage)

    override suspend fun update(id: String, productionBatchInsumoUsage: ProductionBatchInsumoUsage): ProductionBatchInsumoUsage =
        updateRow(SupabaseTables.PRODUCTION_BATCH_INSUMO_USAGES, id, productionBatchInsumoUsage)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PRODUCTION_BATCH_INSUMO_USAGES, id)
}
