package com.didiprogrammer.almacaprina.domain.repository

import com.didiprogrammer.almacaprina.domain.model.ProductRecipeItem

interface ProductRecipeItemRepository {
    suspend fun getAll(): List<ProductRecipeItem>
    suspend fun getById(id: String): ProductRecipeItem?
    suspend fun insert(productRecipeItem: ProductRecipeItem): ProductRecipeItem
    suspend fun update(id: String, productRecipeItem: ProductRecipeItem): ProductRecipeItem
    suspend fun delete(id: String)
}
