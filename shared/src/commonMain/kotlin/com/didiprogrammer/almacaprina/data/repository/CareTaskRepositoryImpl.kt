package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.repository.CareTaskRepository

class CareTaskRepositoryImpl : CareTaskRepository {
    override suspend fun getAll(): List<CareTask> =
        fetchAll(SupabaseTables.CARE_TASKS)

    override suspend fun getById(id: String): CareTask? =
        fetchById(SupabaseTables.CARE_TASKS, id)

    override suspend fun insert(careTask: CareTask): CareTask =
        insertRow(SupabaseTables.CARE_TASKS, careTask)

    override suspend fun update(id: String, careTask: CareTask): CareTask =
        updateRow(SupabaseTables.CARE_TASKS, id, careTask)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.CARE_TASKS, id)
}
