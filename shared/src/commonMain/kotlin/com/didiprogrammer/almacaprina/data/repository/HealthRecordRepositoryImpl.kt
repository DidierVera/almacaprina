package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.repository.HealthRecordRepository

class HealthRecordRepositoryImpl : HealthRecordRepository {
    override suspend fun getAll(): List<HealthRecord> =
        fetchAll(SupabaseTables.HEALTH_RECORDS)

    override suspend fun getById(id: String): HealthRecord? =
        fetchById(SupabaseTables.HEALTH_RECORDS, id)

    override suspend fun insert(healthRecord: HealthRecord): HealthRecord =
        insertRow(SupabaseTables.HEALTH_RECORDS, healthRecord)

    override suspend fun update(id: String, healthRecord: HealthRecord): HealthRecord =
        updateRow(SupabaseTables.HEALTH_RECORDS, id, healthRecord)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.HEALTH_RECORDS, id)
}
