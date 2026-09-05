package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.CareTask

interface CareTaskRepository {
    suspend fun getAll(): List<CareTask>
    suspend fun getById(id: String): CareTask?
    suspend fun insert(careTask: CareTask): CareTask
    suspend fun update(id: String, careTask: CareTask): CareTask
    suspend fun delete(id: String)
}
