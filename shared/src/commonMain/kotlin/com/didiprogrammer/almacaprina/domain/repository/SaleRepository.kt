package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Sale

interface SaleRepository {
    suspend fun getAll(): List<Sale>
    suspend fun getById(id: String): Sale?
    suspend fun insert(sale: Sale): Sale
    suspend fun update(id: String, sale: Sale): Sale
    suspend fun delete(id: String)
}
