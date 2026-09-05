package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Purchase
import com.didiprogrammer.almacaprina.domain.repository.PurchaseRepository

class PurchaseRepositoryImpl : PurchaseRepository {
    override suspend fun getAll(): List<Purchase> =
        fetchAll(SupabaseTables.PURCHASES)

    override suspend fun getById(id: String): Purchase? =
        fetchById(SupabaseTables.PURCHASES, id)

    override suspend fun insert(purchase: Purchase): Purchase =
        insertRow(SupabaseTables.PURCHASES, purchase)

    override suspend fun update(id: String, purchase: Purchase): Purchase =
        updateRow(SupabaseTables.PURCHASES, id, purchase)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PURCHASES, id)
}
