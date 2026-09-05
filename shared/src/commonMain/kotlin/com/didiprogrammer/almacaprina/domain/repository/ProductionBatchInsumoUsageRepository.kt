package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.ProductionBatchInsumoUsage

interface ProductionBatchInsumoUsageRepository {
    suspend fun getAll(): List<ProductionBatchInsumoUsage>
    suspend fun getById(id: String): ProductionBatchInsumoUsage?
    suspend fun insert(productionBatchInsumoUsage: ProductionBatchInsumoUsage): ProductionBatchInsumoUsage
    suspend fun update(id: String, productionBatchInsumoUsage: ProductionBatchInsumoUsage): ProductionBatchInsumoUsage
    suspend fun delete(id: String)
}
