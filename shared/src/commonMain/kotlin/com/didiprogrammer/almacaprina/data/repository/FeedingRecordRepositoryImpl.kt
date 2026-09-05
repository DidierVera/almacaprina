package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.FeedingRecord
import com.didiprogrammer.almacaprina.domain.repository.FeedingRecordRepository

class FeedingRecordRepositoryImpl : FeedingRecordRepository {
    override suspend fun getAll(): List<FeedingRecord> =
        fetchAll(SupabaseTables.FEEDING_RECORDS)

    override suspend fun getById(id: String): FeedingRecord? =
        fetchById(SupabaseTables.FEEDING_RECORDS, id)

    override suspend fun insert(feedingRecord: FeedingRecord): FeedingRecord =
        insertRow(SupabaseTables.FEEDING_RECORDS, feedingRecord)

    override suspend fun update(id: String, feedingRecord: FeedingRecord): FeedingRecord =
        updateRow(SupabaseTables.FEEDING_RECORDS, id, feedingRecord)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.FEEDING_RECORDS, id)
}
