package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent
import com.didiprogrammer.almacaprina.domain.repository.ReproductiveEventRepository

class ReproductiveEventRepositoryImpl : ReproductiveEventRepository {
    override suspend fun getAll(): List<ReproductiveEvent> =
        fetchAll(SupabaseTables.REPRODUCTIVE_EVENTS)

    override suspend fun getById(id: String): ReproductiveEvent? =
        fetchById(SupabaseTables.REPRODUCTIVE_EVENTS, id)

    override suspend fun insert(reproductiveEvent: ReproductiveEvent): ReproductiveEvent =
        insertRow(SupabaseTables.REPRODUCTIVE_EVENTS, reproductiveEvent)

    override suspend fun update(id: String, reproductiveEvent: ReproductiveEvent): ReproductiveEvent =
        updateRow(SupabaseTables.REPRODUCTIVE_EVENTS, id, reproductiveEvent)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.REPRODUCTIVE_EVENTS, id)
}
