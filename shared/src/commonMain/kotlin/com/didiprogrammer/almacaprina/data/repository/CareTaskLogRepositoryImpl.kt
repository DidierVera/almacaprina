package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.CareTaskLog
import com.didiprogrammer.almacaprina.domain.repository.CareTaskLogRepository

class CareTaskLogRepositoryImpl : CareTaskLogRepository {
    override suspend fun getAll(): List<CareTaskLog> =
        fetchAll(SupabaseTables.CARE_TASK_LOGS)

    override suspend fun getById(id: String): CareTaskLog? =
        fetchById(SupabaseTables.CARE_TASK_LOGS, id)

    override suspend fun insert(careTaskLog: CareTaskLog): CareTaskLog =
        insertRow(SupabaseTables.CARE_TASK_LOGS, careTaskLog)

    override suspend fun update(id: String, careTaskLog: CareTaskLog): CareTaskLog =
        updateRow(SupabaseTables.CARE_TASK_LOGS, id, careTaskLog)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.CARE_TASK_LOGS, id)
}
