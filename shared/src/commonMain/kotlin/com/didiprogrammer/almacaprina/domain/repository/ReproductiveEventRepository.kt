package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.ReproductiveEvent

interface ReproductiveEventRepository {
    suspend fun getAll(): List<ReproductiveEvent>
    suspend fun getById(id: String): ReproductiveEvent?
    suspend fun insert(reproductiveEvent: ReproductiveEvent): ReproductiveEvent
    suspend fun update(id: String, reproductiveEvent: ReproductiveEvent): ReproductiveEvent
    suspend fun delete(id: String)
}
