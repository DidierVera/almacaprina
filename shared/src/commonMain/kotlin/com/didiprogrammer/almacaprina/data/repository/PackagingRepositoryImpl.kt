package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Packaging
import com.didiprogrammer.almacaprina.domain.repository.PackagingRepository

class PackagingRepositoryImpl : PackagingRepository {
    override suspend fun getAll(): List<Packaging> =
        fetchAll(SupabaseTables.PACKAGINGS)

    override suspend fun getById(id: String): Packaging? =
        fetchById(SupabaseTables.PACKAGINGS, id)

    override suspend fun insert(packaging: Packaging): Packaging =
        insertRow(SupabaseTables.PACKAGINGS, packaging)

    override suspend fun update(id: String, packaging: Packaging): Packaging =
        updateRow(SupabaseTables.PACKAGINGS, id, packaging)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PACKAGINGS, id)
}
