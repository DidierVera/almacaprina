package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.ProductPackagingOption
import com.didiprogrammer.almacaprina.domain.repository.ProductPackagingOptionRepository

class ProductPackagingOptionRepositoryImpl : ProductPackagingOptionRepository {
    override suspend fun getAll(): List<ProductPackagingOption> =
        fetchAll(SupabaseTables.PRODUCT_PACKAGING_OPTIONS)

    override suspend fun getById(id: String): ProductPackagingOption? =
        fetchById(SupabaseTables.PRODUCT_PACKAGING_OPTIONS, id)

    override suspend fun insert(productPackagingOption: ProductPackagingOption): ProductPackagingOption =
        insertRow(SupabaseTables.PRODUCT_PACKAGING_OPTIONS, productPackagingOption)

    override suspend fun update(id: String, productPackagingOption: ProductPackagingOption): ProductPackagingOption =
        updateRow(SupabaseTables.PRODUCT_PACKAGING_OPTIONS, id, productPackagingOption)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PRODUCT_PACKAGING_OPTIONS, id)
}
