package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.PackagingInventory
import com.didiprogrammer.almacaprina.domain.repository.PackagingInventoryRepository

class PackagingInventoryRepositoryImpl : PackagingInventoryRepository {
    override suspend fun getAll(): List<PackagingInventory> =
        fetchAll(SupabaseTables.PACKAGING_INVENTORIES)

    override suspend fun getById(id: String): PackagingInventory? =
        fetchById(SupabaseTables.PACKAGING_INVENTORIES, id)

    override suspend fun insert(packagingInventory: PackagingInventory): PackagingInventory =
        insertRow(SupabaseTables.PACKAGING_INVENTORIES, packagingInventory)

    override suspend fun update(id: String, packagingInventory: PackagingInventory): PackagingInventory =
        updateRow(SupabaseTables.PACKAGING_INVENTORIES, id, packagingInventory)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PACKAGING_INVENTORIES, id)
}
