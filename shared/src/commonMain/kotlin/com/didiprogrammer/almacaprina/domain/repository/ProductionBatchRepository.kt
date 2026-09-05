package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.ProductionBatch

interface ProductionBatchRepository {
    suspend fun getAll(): List<ProductionBatch>
    suspend fun getById(id: String): ProductionBatch?
    suspend fun insert(productionBatch: ProductionBatch): ProductionBatch
    suspend fun update(id: String, productionBatch: ProductionBatch): ProductionBatch
    suspend fun delete(id: String)
}
