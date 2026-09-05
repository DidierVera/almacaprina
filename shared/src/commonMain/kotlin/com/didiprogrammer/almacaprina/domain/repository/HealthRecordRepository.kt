package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.HealthRecord

interface HealthRecordRepository {
    suspend fun getAll(): List<HealthRecord>
    suspend fun getById(id: String): HealthRecord?
    suspend fun insert(healthRecord: HealthRecord): HealthRecord
    suspend fun update(id: String, healthRecord: HealthRecord): HealthRecord
    suspend fun delete(id: String)
}
