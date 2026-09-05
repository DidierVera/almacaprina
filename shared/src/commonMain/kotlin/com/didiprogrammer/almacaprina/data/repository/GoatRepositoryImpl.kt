package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.repository.GoatRepository

class GoatRepositoryImpl : GoatRepository {
    override suspend fun getAll(): List<Goat> =
        fetchAll(SupabaseTables.GOATS)

    override suspend fun getById(id: String): Goat? =
        fetchById(SupabaseTables.GOATS, id)

    override suspend fun insert(goat: Goat): Goat =
        insertRow(SupabaseTables.GOATS, goat)

    override suspend fun update(id: String, goat: Goat): Goat =
        updateRow(SupabaseTables.GOATS, id, goat)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.GOATS, id)
}
