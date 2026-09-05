package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Packaging

interface PackagingRepository {
    suspend fun getAll(): List<Packaging>
    suspend fun getById(id: String): Packaging?
    suspend fun insert(packaging: Packaging): Packaging
    suspend fun update(id: String, packaging: Packaging): Packaging
    suspend fun delete(id: String)
}
