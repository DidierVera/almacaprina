package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction

interface PackagingDepositTransactionRepository {
    suspend fun getAll(): List<PackagingDepositTransaction>
    suspend fun getById(id: String): PackagingDepositTransaction?
    suspend fun insert(packagingDepositTransaction: PackagingDepositTransaction): PackagingDepositTransaction
    suspend fun update(id: String, packagingDepositTransaction: PackagingDepositTransaction): PackagingDepositTransaction
    suspend fun delete(id: String)
}
