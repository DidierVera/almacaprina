package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.CareTaskLog

interface CareTaskLogRepository {
    suspend fun getAll(): List<CareTaskLog>
    suspend fun getById(id: String): CareTaskLog?
    suspend fun insert(careTaskLog: CareTaskLog): CareTaskLog
    suspend fun update(id: String, careTaskLog: CareTaskLog): CareTaskLog
    suspend fun delete(id: String)
}
