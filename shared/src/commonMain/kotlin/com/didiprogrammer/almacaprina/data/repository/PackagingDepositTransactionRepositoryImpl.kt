package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.PackagingDepositTransaction
import com.didiprogrammer.almacaprina.domain.repository.PackagingDepositTransactionRepository

class PackagingDepositTransactionRepositoryImpl : PackagingDepositTransactionRepository {
    override suspend fun getAll(): List<PackagingDepositTransaction> =
        fetchAll(SupabaseTables.PACKAGING_DEPOSIT_TRANSACTIONS)

    override suspend fun getById(id: String): PackagingDepositTransaction? =
        fetchById(SupabaseTables.PACKAGING_DEPOSIT_TRANSACTIONS, id)

    override suspend fun insert(packagingDepositTransaction: PackagingDepositTransaction): PackagingDepositTransaction =
        insertRow(SupabaseTables.PACKAGING_DEPOSIT_TRANSACTIONS, packagingDepositTransaction)

    override suspend fun update(id: String, packagingDepositTransaction: PackagingDepositTransaction): PackagingDepositTransaction =
        updateRow(SupabaseTables.PACKAGING_DEPOSIT_TRANSACTIONS, id, packagingDepositTransaction)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PACKAGING_DEPOSIT_TRANSACTIONS, id)
}
