package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Breed
import com.didiprogrammer.almacaprina.domain.repository.BreedRepository

class BreedRepositoryImpl : BreedRepository {
    override suspend fun getAll(): List<Breed> =
        fetchAll(SupabaseTables.BREEDS)

    override suspend fun getById(id: String): Breed? =
        fetchById(SupabaseTables.BREEDS, id)

    override suspend fun insert(breed: Breed): Breed =
        insertRow(SupabaseTables.BREEDS, breed)

    override suspend fun update(id: String, breed: Breed): Breed =
        updateRow(SupabaseTables.BREEDS, id, breed)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.BREEDS, id)
}
