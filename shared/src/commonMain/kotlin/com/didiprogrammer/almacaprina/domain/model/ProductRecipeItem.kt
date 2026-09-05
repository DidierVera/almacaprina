package com.didiprogrammer.almacaprina.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductRecipeItem(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("insumo_id") val insumoId: String,
    @SerialName("quantity_per_output_unit") val quantityPerOutputUnit: Double
)
