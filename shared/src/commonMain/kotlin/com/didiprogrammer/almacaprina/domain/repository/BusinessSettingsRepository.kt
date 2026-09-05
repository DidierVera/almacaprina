package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.BusinessSettings

interface BusinessSettingsRepository {
    suspend fun getAll(): List<BusinessSettings>
    suspend fun getById(id: String): BusinessSettings?
    suspend fun insert(businessSettings: BusinessSettings): BusinessSettings
    suspend fun update(id: String, businessSettings: BusinessSettings): BusinessSettings
    suspend fun delete(id: String)
}
