package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Insumo

interface InsumoRepository {
    suspend fun getAll(): List<Insumo>
    suspend fun getById(id: String): Insumo?
    suspend fun insert(insumo: Insumo): Insumo
    suspend fun update(id: String, insumo: Insumo): Insumo
    suspend fun delete(id: String)
}
