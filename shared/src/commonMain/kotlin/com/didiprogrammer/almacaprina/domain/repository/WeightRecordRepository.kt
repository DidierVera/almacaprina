package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.WeightRecord

interface WeightRecordRepository {
    suspend fun getAll(): List<WeightRecord>
    suspend fun getById(id: String): WeightRecord?
    suspend fun insert(weightRecord: WeightRecord): WeightRecord
    suspend fun update(id: String, weightRecord: WeightRecord): WeightRecord
    suspend fun delete(id: String)
}
