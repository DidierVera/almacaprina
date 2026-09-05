package com.didiprogrammer.almacaprina.data.repository

import com.didiprogrammer.almacaprina.data.remote.SupabaseTables
import com.didiprogrammer.almacaprina.data.remote.deleteRow
import com.didiprogrammer.almacaprina.data.remote.fetchAll
import com.didiprogrammer.almacaprina.data.remote.fetchById
import com.didiprogrammer.almacaprina.data.remote.insertRow
import com.didiprogrammer.almacaprina.data.remote.updateRow
import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem
import com.didiprogrammer.almacaprina.domain.repository.ProductRecipeItemRepository

class ProductRecipeItemRepositoryImpl : ProductRecipeItemRepository {
    override suspend fun getAll(): List<ProductRecipeItem> =
        fetchAll(SupabaseTables.PRODUCT_RECIPE_ITEMS)

    override suspend fun getById(id: String): ProductRecipeItem? =
        fetchById(SupabaseTables.PRODUCT_RECIPE_ITEMS, id)

    override suspend fun insert(productRecipeItem: ProductRecipeItem): ProductRecipeItem =
        insertRow(SupabaseTables.PRODUCT_RECIPE_ITEMS, productRecipeItem)

    override suspend fun update(id: String, productRecipeItem: ProductRecipeItem): ProductRecipeItem =
        updateRow(SupabaseTables.PRODUCT_RECIPE_ITEMS, id, productRecipeItem)

    override suspend fun delete(id: String) =
        deleteRow(SupabaseTables.PRODUCT_RECIPE_ITEMS, id)
}
