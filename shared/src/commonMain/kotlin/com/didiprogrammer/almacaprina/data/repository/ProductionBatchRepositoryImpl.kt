package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.ProductionBatch
import com.didiprogrammer.almacaprina.domain.repository.ProductionBatchRepository

class ProductionBatchRepositoryImpl : ProductionBatchRepository {
    override suspend fun getAll(): List<ProductionBatch> =
        fetchAll(SupabaseTables.PRODUCTION_BATCHES)

    override suspend fun getById(id: String): ProductionBatch? =
        fetchById(SupabaseTables.PRODUCTION_BATCHES, id)

    override suspend fun insert(productionBatch: ProductionBatch): ProductionBatch =
        insertRow(SupabaseTables.PRODUCTION_BATCHES, productionBatch)

    override suspend fun update(id: String, productionBatch: ProductionBatch): ProductionBatch =
        updateRow(SupabaseTables.PRODUCTION_BATCHES, id, productionBatch)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PRODUCTION_BATCHES, id)
}
