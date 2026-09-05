package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Purchase

interface PurchaseRepository {
    suspend fun getAll(): List<Purchase>
    suspend fun getById(id: String): Purchase?
    suspend fun insert(purchase: Purchase): Purchase
    suspend fun update(id: String, purchase: Purchase): Purchase
    suspend fun delete(id: String)
}
