package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.WeightRecord
import com.didiprogrammer.almacaprina.domain.repository.WeightRecordRepository

class WeightRecordRepositoryImpl : WeightRecordRepository {
    override suspend fun getAll(): List<WeightRecord> =
        fetchAll(SupabaseTables.WEIGHT_RECORDS)

    override suspend fun getById(id: String): WeightRecord? =
        fetchById(SupabaseTables.WEIGHT_RECORDS, id)

    override suspend fun insert(weightRecord: WeightRecord): WeightRecord =
        insertRow(SupabaseTables.WEIGHT_RECORDS, weightRecord)

    override suspend fun update(id: String, weightRecord: WeightRecord): WeightRecord =
        updateRow(SupabaseTables.WEIGHT_RECORDS, id, weightRecord)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.WEIGHT_RECORDS, id)
}
