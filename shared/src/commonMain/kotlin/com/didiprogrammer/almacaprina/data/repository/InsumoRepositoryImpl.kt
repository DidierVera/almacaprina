package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Insumo
import com.didiprogrammer.almacaprina.domain.repository.InsumoRepository

class InsumoRepositoryImpl : InsumoRepository {
    override suspend fun getAll(): List<Insumo> =
        fetchAll(SupabaseTables.INSUMOS)

    override suspend fun getById(id: String): Insumo? =
        fetchById(SupabaseTables.INSUMOS, id)

    override suspend fun insert(insumo: Insumo): Insumo =
        insertRow(SupabaseTables.INSUMOS, insumo)

    override suspend fun update(id: String, insumo: Insumo): Insumo =
        updateRow(SupabaseTables.INSUMOS, id, insumo)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.INSUMOS, id)
}
