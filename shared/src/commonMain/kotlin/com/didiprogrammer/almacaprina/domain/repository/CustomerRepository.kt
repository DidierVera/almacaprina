package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.Customer

interface CustomerRepository {
    suspend fun getAll(): List<Customer>
    suspend fun getById(id: String): Customer?
    suspend fun insert(customer: Customer): Customer
    suspend fun update(id: String, customer: Customer): Customer
    suspend fun delete(id: String)
}
