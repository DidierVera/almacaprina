package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.FeedingRecord

interface FeedingRecordRepository {
    suspend fun getAll(): List<FeedingRecord>
    suspend fun getById(id: String): FeedingRecord?
    suspend fun insert(feedingRecord: FeedingRecord): FeedingRecord
    suspend fun update(id: String, feedingRecord: FeedingRecord): FeedingRecord
    suspend fun delete(id: String)
}
