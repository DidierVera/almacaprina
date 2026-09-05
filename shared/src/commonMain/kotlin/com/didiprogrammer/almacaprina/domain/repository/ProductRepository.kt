package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Product

interface ProductRepository {
    suspend fun getAll(): List<Product>
    suspend fun getById(id: String): Product?
    suspend fun insert(product: Product): Product
    suspend fun update(id: String, product: Product): Product
    suspend fun delete(id: String)
}
