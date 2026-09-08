package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption

interface ProductPackagingOptionRepository {
    suspend fun getAll(): List<ProductPackagingOption>
    suspend fun getById(id: String): ProductPackagingOption?
    suspend fun insert(productPackagingOption: ProductPackagingOption): ProductPackagingOption
    suspend fun update(id: String, productPackagingOption: ProductPackagingOption): ProductPackagingOption
    suspend fun delete(id: String)
}
