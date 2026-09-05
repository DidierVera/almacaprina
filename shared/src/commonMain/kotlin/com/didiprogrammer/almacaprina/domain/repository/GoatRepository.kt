package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Goat

interface GoatRepository {
    suspend fun getAll(): List<Goat>
    suspend fun getById(id: String): Goat?
    suspend fun insert(goat: Goat): Goat
    suspend fun update(id: String, goat: Goat): Goat
    suspend fun delete(id: String)
}
