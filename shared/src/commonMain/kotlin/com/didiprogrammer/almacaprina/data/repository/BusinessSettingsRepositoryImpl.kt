package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.BusinessSettings
import com.didiprogrammer.almacaprina.domain.repository.BusinessSettingsRepository

class BusinessSettingsRepositoryImpl : BusinessSettingsRepository {
    override suspend fun getAll(): List<BusinessSettings> =
        fetchAll(SupabaseTables.BUSINESS_SETTINGS)

    override suspend fun getById(id: String): BusinessSettings? =
        fetchById(SupabaseTables.BUSINESS_SETTINGS, id)

    override suspend fun insert(businessSettings: BusinessSettings): BusinessSettings =
        insertRow(SupabaseTables.BUSINESS_SETTINGS, businessSettings)

    override suspend fun update(id: String, businessSettings: BusinessSettings): BusinessSettings =
        updateRow(SupabaseTables.BUSINESS_SETTINGS, id, businessSettings)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.BUSINESS_SETTINGS, id)
}
