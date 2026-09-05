package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import com.didiprogrammer.almacaprina.domain.repository.MilkProductionRecordRepository

class MilkProductionRecordRepositoryImpl : MilkProductionRecordRepository {
    override suspend fun getAll(): List<MilkProductionRecord> =
        fetchAll(SupabaseTables.MILK_PRODUCTION_RECORDS)

    override suspend fun getById(id: String): MilkProductionRecord? =
        fetchById(SupabaseTables.MILK_PRODUCTION_RECORDS, id)

    override suspend fun insert(milkProductionRecord: MilkProductionRecord): MilkProductionRecord =
        insertRow(SupabaseTables.MILK_PRODUCTION_RECORDS, milkProductionRecord)

    override suspend fun update(id: String, milkProductionRecord: MilkProductionRecord): MilkProductionRecord =
        updateRow(SupabaseTables.MILK_PRODUCTION_RECORDS, id, milkProductionRecord)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.MILK_PRODUCTION_RECORDS, id)
}
