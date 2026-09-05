package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Sale
import com.didiprogrammer.almacaprina.domain.repository.SaleRepository

class SaleRepositoryImpl : SaleRepository {
    override suspend fun getAll(): List<Sale> =
        fetchAll(SupabaseTables.SALES)

    override suspend fun getById(id: String): Sale? =
        fetchById(SupabaseTables.SALES, id)

    override suspend fun insert(sale: Sale): Sale =
        insertRow(SupabaseTables.SALES, sale)

    override suspend fun update(id: String, sale: Sale): Sale =
        updateRow(SupabaseTables.SALES, id, sale)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.SALES, id)
}
