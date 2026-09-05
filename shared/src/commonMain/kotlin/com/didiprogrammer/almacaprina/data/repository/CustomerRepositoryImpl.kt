package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.Customer
import com.didiprogrammer.almacaprina.domain.repository.CustomerRepository

class CustomerRepositoryImpl : CustomerRepository {
    override suspend fun getAll(): List<Customer> =
        fetchAll(SupabaseTables.CUSTOMERS)

    override suspend fun getById(id: String): Customer? =
        fetchById(SupabaseTables.CUSTOMERS, id)

    override suspend fun insert(customer: Customer): Customer =
        insertRow(SupabaseTables.CUSTOMERS, customer)

    override suspend fun update(id: String, customer: Customer): Customer =
        updateRow(SupabaseTables.CUSTOMERS, id, customer)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.CUSTOMERS, id)
}
