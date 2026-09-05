package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord

interface MilkProductionRecordRepository {
    suspend fun getAll(): List<MilkProductionRecord>
    suspend fun getById(id: String): MilkProductionRecord?
    suspend fun insert(milkProductionRecord: MilkProductionRecord): MilkProductionRecord
    suspend fun update(id: String, milkProductionRecord: MilkProductionRecord): MilkProductionRecord
    suspend fun delete(id: String)
}
