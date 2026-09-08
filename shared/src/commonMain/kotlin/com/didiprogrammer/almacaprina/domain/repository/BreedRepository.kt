package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Breed

interface BreedRepository {
    suspend fun getAll(): List<Breed>
    suspend fun getById(id: String): Breed?
    suspend fun insert(breed: Breed): Breed
    suspend fun update(id: String, breed: Breed): Breed
    suspend fun delete(id: String)
}
