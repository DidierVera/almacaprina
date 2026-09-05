package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Product
import com.didiprogrammer.almacaprina.domain.repository.ProductRepository

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getAll(): List<Product> =
        fetchAll(SupabaseTables.PRODUCTS)

    override suspend fun getById(id: String): Product? =
        fetchById(SupabaseTables.PRODUCTS, id)

    override suspend fun insert(product: Product): Product =
        insertRow(SupabaseTables.PRODUCTS, product)

    override suspend fun update(id: String, product: Product): Product =
        updateRow(SupabaseTables.PRODUCTS, id, product)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PRODUCTS, id)
}
