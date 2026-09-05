package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.PackagingInventory

interface PackagingInventoryRepository {
    suspend fun getAll(): List<PackagingInventory>
    suspend fun getById(id: String): PackagingInventory?
    suspend fun insert(packagingInventory: PackagingInventory): PackagingInventory
    suspend fun update(id: String, packagingInventory: PackagingInventory): PackagingInventory
    suspend fun delete(id: String)
}
